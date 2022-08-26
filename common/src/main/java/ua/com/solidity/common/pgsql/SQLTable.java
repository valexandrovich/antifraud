package ua.com.solidity.common.pgsql;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.com.solidity.common.*;
import ua.com.solidity.common.data.*;

import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

@CustomLog
public class SQLTable {
    public final String tableName;
    public final JdbcTemplate template;
    private final Map<String, SQLField> fields;
    final List<SQLField> mappedFields = new ArrayList<>();
    private ArgumentSetter setter;
    private BatchAction reset;
    private BatchAction add;
    private InsertBatch insertBatch;
    private final SQLFlushMode mode;
    private String queryHeaderStr;
    private String statementStr;
    private String copyHeader;
    private DataExtensionFactory extensionFactory = null;
    private Connection connection = null;
    private boolean singleTransaction = false;
    CopyManager copyManager = null;
    PreparedStatement statement = null;
    private final CacheSize size;
    private boolean prepared = false;

    private boolean hasTotalErrors = false;

    private OutputCache outputCache = null;
    DataBatch.ErrorHandler errorHandler = null;

    private interface ArgumentSetter {
        ErrorResult setArguments(DataObject obj, DataExtensionFactory factory);
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

    public final void setCache(OutputCache cache) {
        outputCache = cache;
    }

    public static Connection connectionNeeded() {
        Connection connection;
        try {
            connection = DBUtils.createConnection("spring.datasource", "reWriteBatchedInserts=true");
            return connection;
        } catch (Exception e) {
            log.error("DBConnection not established.", e);
        }
        return null;
    }

    @SuppressWarnings("unused")
    protected final DataExtensionFactory getExtensionFactory(DataBatch batch) {
        DataExtensionFactory batchExtensionFactory = batch.getExtensionFactory();
        if (extensionFactory == batchExtensionFactory) return extensionFactory;
        if (extensionFactory == null) {
            extensionFactory = batch.getExtensionFactory();
        }
        return extensionFactory;
    }

    private void prepareConnection() {
        if (singleTransaction) {
            try {
                connection.setAutoCommit(false);
            } catch (Exception e) {
                singleTransaction = false;
            }
        }
    }

    private void initObjects(CacheSize size) {
        if (mode == SQLFlushMode.COPY_BUFFER) {
            insertBatch = InsertBatch.allocate(size.size, size.rowCacheSize, queryHeaderStr, this::handleInsertBatch);
        }

        connection = connectionNeeded();

        if (connection != null) {
            prepareConnection();
            if (mode == SQLFlushMode.PREPARED_STATEMENT_BATCH) {
                try {
                    statement = connection.prepareStatement(statementStr);
                } catch (Exception e) {
                    log.error("PreparedStatement not created.", e);
                }
            } else {
                try {
                    copyManager = new CopyManager((BaseConnection) connection);
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
        log.info("$table$Query execution time for {} rows: {}", rowCount, printer.getDurationString());
        return (int) count;
    }

    public static SQLTable create(String tableName, SQLFlushMode mode, JsonNode mapping, boolean singleTransaction, int cacheSize, int rowCacheSize) {
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
                table.singleTransaction = singleTransaction;
                if (table.isValidTargetTable()) {
                    return table;
                }
                table.close();
            } else {
                log.error("Table \"{}\" not found.", tableName);
            }
        }
        return null;
    }

    private ErrorResult argumentsSetInBatchMode(DataObject obj, DataExtensionFactory factory) {
        ErrorResult res = new ErrorResult();

        if (factory != null && !factory.assignInsertBatch(obj, insertBatch)) {
            return res;
        }

        boolean hasErrors = false;

        StringBuilder errorBuilder = new StringBuilder();

        for (SQLField field : mappedFields) {
            SQLError error = field.putArgument(insertBatch, obj);
            if (error != null) {
                hasErrors = true;
                res.add(obj, tableName, error.getMessage());
                if (log.isDebugEnabled()) {
                    if (errorBuilder.length() == 0) {
                        errorBuilder.append(error.getMessage());
                    } else errorBuilder.append("; ").append(error.getMessage());
                }
            }
        }

        if (hasErrors) {
            log.info("$table$Row content error: {}", errorBuilder);
            this.hasTotalErrors = true;
            return res;
        }
        return null;
    }

    private ErrorResult argumentsSetInPreparedStatementMode(DataObject obj, DataExtensionFactory factory) {
        ErrorResult res = new ErrorResult();
        StringBuilder errorBuilder = new StringBuilder();
        int paramIndex = 0;
        if (factory != null && (paramIndex = factory.assignStatementArgs(obj, statement)) < 0) {
            return res;
        }
        try {
            boolean hasErrors = false;
            for (SQLField field : mappedFields) {
                SQLError error = field.putArgument(statement, ++paramIndex, obj);
                if (error != null) {
                    hasErrors = true;
                    res.add(obj, tableName, error.getMessage());
                    if (errorBuilder.length() == 0) {
                        errorBuilder.append(error.getMessage());
                    } else errorBuilder.append("; ").append(error.getMessage());
                }
            }
            if (hasErrors) {
                log.info("$table$Row content error. {}", errorBuilder);
                this.hasTotalErrors = true;
            }
        } catch (Exception e) {
            log.error("Prepared statement field assign error.", e);
            res.error();
        }
        return res;
    }

    private boolean batchResetInBatchMode() {
        insertBatch.resetBatch();
        return true;
    }

    private boolean batchAddInBatchMode() {
        return insertBatch.addBatch();
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

    public static void executeStatement(PreparedStatement preparedStatement, DataBatch batch, String sourceName) {
        try {
            int[] res = preparedStatement.executeBatch();
            int index = 0;
            for (DataObject obj : batch.validObjects()) {
                long r = res[index++];
                if (r != preparedStatement.SUCCESS_NO_INFO && r <= 0) {
                    ErrorResult err = new ErrorResult();
                    err.add(obj, sourceName, "Database insert error.");
                    obj.setErrorResult(err);
                }
            }
        } catch (Exception e) {
            log.error("Can't execute statement", e);
        }
    }

    private ErrorResult handleObject(DataObject obj) {
        ErrorResult res = setter.setArguments(obj, extensionFactory);
        if (!res.isErrorState() && !add.exec()) {
            res.error();
        }

        if (res.isErrorState()) {
            obj.setErrorResult(res);
            reset.exec();
        }

        return res;
    }

    private void processBatch() {
        DataBatch batch = outputCache.getBatch();

        if (batch.getExtensionFactory() != extensionFactory) {
            if (extensionFactory == null) {
                log.error("SQLTable extensionFactory not initialized.");
            } else {
                log.error("SQLTable {} extensionFactory differs from assigned in dataBatch.", tableName);
            }
            return;
        }

        batch.handle(this::handleObject, errorHandler);

        if (mode == SQLFlushMode.PREPARED_STATEMENT_BATCH) {
            executeStatement(statement, batch, tableName);
        } else {
            this.insertBatch.flush();
        }
    }

    public final boolean isValid() {
        if (template == null || fields.isEmpty() || (insertBatch == null && mode != SQLFlushMode.PREPARED_STATEMENT_BATCH)) return false;
        for (var entry : fields.entrySet()) {
            if (!entry.getValue().isValid()) return false;
        }
        return true;
    }

    private String checkField(FieldDescription desc) {
        SQLField field = fields.getOrDefault(desc.name, null);
        if (field == null) {
            return Utils.messageFormat("Field not found \"{}\"", desc.name);
        }

        if (field.getMapping() != null) {
            return Utils.messageFormat("Field \"{}\" already mapped.", desc.name);
        }

        if (!(field.getType().equals(desc.type) || field.getType2().equals(desc.type) || (field.getSqlType() == desc.sqlType && desc.sqlType != null))) {
            return Utils.messageFormat("Field \"{}\" type ({}) is incompatible with table field type ({}).", desc.name, desc.type, field.getFieldDescription());
        }

        return null;
    }

    private boolean extensionFactoryIsInvalid() {
        List<FieldDescription> extensionFactoryFields = extensionFactory.getFields();
        boolean invalid = false;
        for (FieldDescription desc : extensionFactoryFields) {
            String errorMsg;
            if ((errorMsg = checkField(desc)) != null) {
                log.error("$table${}: {}", tableName, errorMsg);
                invalid = true;
            }
        }
        return invalid;
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

    private void secondaryInitMapping() {
        if (mappedFields.isEmpty()) {
            for (var entry: fields.entrySet()) {
                SQLField field = entry.getValue();
                if (field.getMapping() == null && !extensionFactory.fieldExists(field.getName())) {
                    field.setMapping(new SQLFieldMapping(field.getName(), null));
                    mappedFields.add(field);
                }
            }
        }
    }

    public final boolean prepareTable(DataExtensionFactory factory, OutputCache cache, DataBatch.ErrorHandler errorHandler) {
        if (prepared || extensionFactory != null || factory == null || cache == null) return false;
        extensionFactory = factory;
        if (extensionFactoryIsInvalid()) {
            extensionFactory = null;
            return false;
        }
        outputCache = cache;
        this.errorHandler = errorHandler;
        secondaryInitMapping();
        initQuery();
        initObjects(size);
        prepared = true;
        return true;
    }

    public final void handleBatch() {
        if (!prepared) {
            log.error("Table not prepared with extension factory and output cache.");
            return;
        }
        processBatch();
    }

    private void addFieldToErrorBuilder(StringBuilder builder, String fieldName, long count) {
        if (builder.length() > 0) {
            builder.append("; ");
        }
        builder.append(fieldName).append("(").append(count).append(")");
    }

    private void logErrors(StringBuilder builder, SQLAssignResult res) {
        if (builder.length() == 0) return;
        log.info("$table$Errors with message \"{}\" found for fields: {}", res.getMessage(), builder);
    }

    public final void logInsertErrors() {
        if (!hasTotalErrors) {
            log.info("$table$Insert errors not found, OK.");
            return;
        }
        StringBuilder nullBuilder = new StringBuilder();
        StringBuilder exceptionBuilder = new StringBuilder();
        List<String> lengthErrors = new ArrayList<>();

        for (SQLField field: mappedFields) {
            if (field.getNullErrorsFound() > 0) {
                addFieldToErrorBuilder(nullBuilder, field.getName(), field.getNullErrorsFound());
            }

            if (field.getExceptionsFound() > 0) {
                addFieldToErrorBuilder(exceptionBuilder, field.getName(), field.getExceptionsFound());
            }

            if (field.getLengthErrorsFound() > 0) {
                lengthErrors.add(Utils.messageFormat("  {}, error row count: {}, Max size detected {}, at value \"{}\"",
                        field.getFieldDescription(), field.getLengthErrorsFound(), field.getMaxLengthReached(), field.getMaxLengthStr()));
            }
        }

        if (log.isDebugEnabled()) {
            logErrors(nullBuilder, SQLAssignResult.NULL_NOT_ALLOWED);
            logErrors(exceptionBuilder, SQLAssignResult.EXCEPTION);
        }

        if (!lengthErrors.isEmpty()) {
            log.info("\"{}\" found for fields:", SQLAssignResult.LENGTH_ERROR.getMessage());
            for (String error : lengthErrors) {
                log.info("$table${}", error);
            }
        }
    }

    public void close() {
        if (insertBatch == null && statement == null && connection == null) return; // not prepared guarantee
        insertBatch = null;
        if (statement != null) {
            try {
                statement.close();
                if (singleTransaction) {
                    connection.commit();
                }
            } catch (Exception e) {
                // nothing
            }
            statement = null;
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                connection = null;
            }
        }
        prepared = false;
        if (log.isDebugEnabled()) {
            logInsertErrors();
        }
    }
}
