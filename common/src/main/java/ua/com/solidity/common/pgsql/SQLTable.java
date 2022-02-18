package ua.com.solidity.common.pgsql;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.com.solidity.common.DBUtils;
import ua.com.solidity.common.DurationPrinter;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.data.DataExtensionFactory;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.data.FieldDescription;

import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
public class SQLTable {
    public static final String FIELD_ID = "id";
    public static final String FIELD_REVISION = "revision";
    public static final String FIELD_PARENT = "parent";
    public static final String FIELD_EXTRA_ID = "extra_id";

    static final String[] specialFields = new String[]{
            SQLTable.FIELD_ID,
            SQLTable.FIELD_REVISION,
            SQLTable.FIELD_PARENT,
            SQLTable.FIELD_EXTRA_ID
    };

    private static Connection connection = null;

    public final String tableName;
    public final JdbcTemplate template;
    private final Map<String, SQLField> fields;
    final List<SQLField> mappedFields = new ArrayList<>();
    private ArgumentSetter setter;
    private BatchAction reset;
    private BatchAction add;
    private InsertBatch batch;
    private final SQLFlushMode mode;
    private String queryHeaderStr;
    private String statementStr;
    private String copyHeader;
    private DataExtensionFactory extensionFactory = null;
    CopyManager copyManager = null;
    PreparedStatement statement = null;
    private final CacheSize size;

    private interface ArgumentSetter {
        boolean setArguments(DataObject obj, DataExtensionFactory factory);
    }

    private interface BatchAction {
        boolean exec();
    }

    private static class CacheSize {
        public final int size;
        public final int rowCacheSize;
        public CacheSize(int cacheSize, int rowCacheSize) {
            this.size = cacheSize;
            this.rowCacheSize = rowCacheSize;
        }
    }

    private SQLTable(String tableName, SQLFlushMode mode, JsonNode mapping, JdbcTemplate template,
                     Map<String, SQLField> fields, CacheSize size) {
        this.tableName = tableName;
        this.template = template;
        this.fields = fields;
        this.mode = mode;
        this.size = size;
        initMode();
        initMapping(mapping);
    }

    public static Connection connectionNeeded() {
        if (connection != null) return connection;
        try {
            connection = DBUtils.createConnection("spring.datasource", "reWriteBatchedInserts=true");
            assert connection != null;
        } catch (Exception e) {
            log.error("DBConnection not established.");
        }
        return connection;
    }

    protected final DataExtensionFactory getExtensionFactory(DataBatch batch) {
        DataExtensionFactory batchExtensionFactory = batch.getExtensionFactory();
        if (extensionFactory == batchExtensionFactory) return extensionFactory;
        if (extensionFactory == null) {
            extensionFactory = batch.getExtensionFactory();
        }
        return extensionFactory;
    }

    private void initObjects(CacheSize size) {
        if (mode == SQLFlushMode.INSERT_BATCH_BUFFER || mode == SQLFlushMode.COPY_BUFFER) {
            batch = InsertBatch.allocate(size.size, size.rowCacheSize, queryHeaderStr, this::handleInsertBatch);
        }

        Connection conn = null;

        if (mode == SQLFlushMode.COPY_BUFFER || mode == SQLFlushMode.PREPARED_STATEMENT_BATCH) {
            conn = connectionNeeded();
        }

        if (conn != null) {
            if (mode == SQLFlushMode.PREPARED_STATEMENT_BATCH) {
                try {
                    statement = conn.prepareStatement(statementStr);
                } catch (Exception e) {
                    log.error("PreparedStatement not created.", e);
                }
            } else {
                try {
                    copyManager = new CopyManager((BaseConnection) conn);
                } catch (Exception e) {
                    log.error("CopyManager not created.", e);
                }
            }
        }
    }

    private int handleInsertBatch(InsertBatch batch, CharBuffer buffer, int rowCount) {
        long count;
        DurationPrinter printer = new DurationPrinter();
        if (mode == SQLFlushMode.COPY_BUFFER) {
            try {
                count = copyManager.copyIn(copyHeader, batch.getReader());
            } catch (Exception e) {
                count = 0;
            }
        } else {
            template.execute(buffer.toString());
            count = rowCount;
        }
        printer.stop();
        log.info("Query execution time for {} rows: {}", rowCount, printer.getDurationString());
        return (int) count;
    }

    public static SQLTable create(String tableName, SQLFlushMode mode, JsonNode mapping, int cacheSize, int rowCacheSize) {
        if (Utils.checkApplicationContext()) {
            JdbcTemplate template = Utils.getApplicationContext().getBean(JdbcTemplate.class);

            Map<String, SQLField> fields;
            String value = template.queryForObject(String.format("select get_table_info('%s','%s','%s')", "otp", "public", tableName), String.class);
            JsonNode node = Utils.getJsonNode(value);
            if (node != null && node.isArray()) {
                fields = new HashMap<>();
                for (int i = 0; i < node.size(); ++i) {
                    SQLField field = Utils.jsonToValue(node.get(i), SQLField.class);
                    if (field != null) {
                        fields.put(field.getName(), field);
                    }
                }
                SQLTable table = new SQLTable(tableName, mode, mapping, template, fields, new CacheSize(cacheSize, rowCacheSize));
                if (table.isValidTargetTable()) {
                    return table;
                }
                table.close();
            }
        }
        return null;
    }

    private boolean argumentsSetInBatchMode(DataObject obj, DataExtensionFactory factory) {
        if (factory != null && !factory.assignInsertBatch(obj, batch)) return false;
        StringBuilder errorBuilder = new StringBuilder();
        for (SQLField field : mappedFields) {
            SQLError error = field.putArgument(batch, obj);
            if (error != null) {
                if (errorBuilder.length() == 0) {
                    errorBuilder.append(error.getMessage());
                } else errorBuilder.append("; ").append(error.getMessage());
            }
        }
        if (errorBuilder.length() > 0) {
            log.error("-Row content error: {}", errorBuilder);
            errorBuilder.setLength(0);
            return false;
        }
        return true;
    }

    private boolean argumentsSetInPreparedStatementMode(DataObject obj, DataExtensionFactory factory) {
        StringBuilder errorBuilder = new StringBuilder();
        int paramIndex = 0;
        if (factory != null && (paramIndex = factory.assignStatementArgs(obj, statement)) < 0) return false;
        try {
            for (SQLField field : mappedFields) {
                SQLError error = field.putArgument(statement, ++paramIndex, obj);
                if (error != null) {
                    if (errorBuilder.length() == 0) {
                        errorBuilder.append(error.getMessage());
                    } else errorBuilder.append("; ").append(error.getMessage());
                }
            }
            if (errorBuilder.length() > 0) {
                log.error("----Row content error. {}", errorBuilder);
                errorBuilder.setLength(0);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("--Prepare statement error. {}: {}.", e.getCause().getClass().getName(), e.getCause().getMessage());
            return false;
        }
    }

    private boolean batchResetInBatchMode() {
        batch.resetBatch();
        return true;
    }

    private boolean batchAddInBatchMode() {
        return batch.addBatch();
    }

    private boolean batchAddInPreparedStatementMode() {
        try {
            statement.addBatch();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean batchResetInPreparedStatementMode() {
        try {
            statement.clearParameters();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static int executeStatement(PreparedStatement statement) {
        int count = 0;
        try {
            int[] res = statement.executeBatch();
            for (int r : res) {
                if (r != 0) {
                    ++count;
                }
            }
        } catch (Exception e) {
            log.error("Can't execute statement", e);
        }
        return count;
    }

    private int afterPreparedStatementExec() {
        return executeStatement(statement);
    }

    private int assign(DataBatch batch) {
        if (batch.getExtensionFactory() != extensionFactory) {
            log.error("SQLTable extensionFactory differs from assigned in dataBatch.");
            return 0;
        }
        int count = 0;
        int batchSize = batch.getObjectCount();
        for (int i = 0; i < batchSize; ++i) {
            DataObject obj = batch.get(i);
            if (setter.setArguments(obj, extensionFactory)) {
                if (add.exec()) {
                    ++count;
                }
            } else {
                reset.exec();
            }
        }
        if (mode == SQLFlushMode.PREPARED_STATEMENT_BATCH) {
            count = afterPreparedStatementExec();
        }
        return count;
    }

    public final boolean isValid() {
        if (template == null || fields.isEmpty() || (batch == null && mode != SQLFlushMode.PREPARED_STATEMENT_BATCH)) return false;
        for (var entry : fields.entrySet()) {
            if (!entry.getValue().isValid()) return false;
        }
        return true;
    }

    private boolean checkField(String name, Class<?> type) {
        SQLField field = fields.getOrDefault(name, null);
        return !(field == null || field.getSqlType() == null || field.getSqlType().getClass() != type ||
                field.getMapping() != null);
    }

    private boolean checkExtensionFactory() {
        List<FieldDescription> extensionFactoryFields = extensionFactory.getFields();
        for (FieldDescription desc : extensionFactoryFields) {
            if (!checkField(desc.name, desc.sqlType.getClass())) return false;
        }
        return true;
    }

    public final boolean isValidTargetTable() {
        return isValid();
    }

    private void initMode() {
        if (mode != SQLFlushMode.PREPARED_STATEMENT_BATCH) {
            setter = this::argumentsSetInBatchMode;
            add = this::batchAddInBatchMode;
            reset = this::batchResetInBatchMode;
        } else {
            setter = this::argumentsSetInPreparedStatementMode;
            add = this::batchAddInPreparedStatementMode;
            reset = this::batchResetInPreparedStatementMode;
        }
    }

    private String makeInsertHeader() {
        return "insert into " + makeTableNameAndFieldList() + " values ";
    }

    private String makeStatement() {
        return makeInsertHeader() + makeValues();
    }

    private String makeTableNameAndFieldList() {
        StringBuilder builder = new StringBuilder();
        boolean firstField = true;
        builder.append("\"").append(tableName).append("\"(");
        List<FieldDescription> fieldDescriptionList = extensionFactory.getFields();
        for (FieldDescription fieldDescription : fieldDescriptionList) {
            if (!firstField) {
                builder.append(", ");
            }
            builder.append(fieldDescription.name);
            firstField = false;
        }
        for (SQLField field : mappedFields) {
            if (!firstField) {
                builder.append(", ");
            }
            builder.append(field.getName());
            firstField = false;
        }
        return builder.append(")").toString();
    }

    private String makeValues() {
        boolean firstField = true;
        StringBuilder builder = new StringBuilder().append("(");
        List<FieldDescription> fieldDescriptionList = extensionFactory.getFields();
        for (FieldDescription fieldDescription : fieldDescriptionList) {
            if (!firstField) {
                builder.append(", ");
            }
            builder.append(SQLField.getArgStringByType(fieldDescription.sqlType, fieldDescription.type));
            firstField = false;
        }

        for (SQLField field : mappedFields) {
            if (!firstField) {
                builder.append(", ");
            }
            builder.append(field.getArgString());
            firstField = false;
        }
        return builder.append(")").toString();
    }

    private String makeCopySQL() {
        return "COPY " + makeTableNameAndFieldList() +
                " FROM STDIN WITH(FORMAT CSV, DELIMITER ',', NULL 'null', QUOTE '\"', ESCAPE '\"', ENCODING 'UTF-8')";
    }

    private void initQuery() {
        queryHeaderStr = makeInsertHeader();
        statementStr = makeStatement();
        copyHeader = makeCopySQL();
    }

    private void initMapping(JsonNode mapping) {
        if (mapping != null && mapping.isObject()) {
            for (var iterator = mapping.fields(); iterator.hasNext();) {
                var entry = iterator.next();
                String fieldName = entry.getKey();
                SQLField field = fields.getOrDefault(fieldName, null);
                if (field != null) {
                    field.setMapping(new SQLFieldMapping(fieldName, entry.getValue()));
                    mappedFields.add(field);
                }
            }
        }
    }

    public final int doOutput(OutputCache cache) {
        if (extensionFactory == null) {
            getExtensionFactory(cache.getBatch());
            if (!checkExtensionFactory()) {
                log.error("Extension Factory is incompatible with table {}.", tableName);
            }
            initQuery();
            initObjects(size);
        }
        return assign(cache.getBatch());
    }

    public void close() {
        if (batch != null) {
            batch.flush();
            batch = null;
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
                // nothing
            }
            statement = null;
        }
    }
}
