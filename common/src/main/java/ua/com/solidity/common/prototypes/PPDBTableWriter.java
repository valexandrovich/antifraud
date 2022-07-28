package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.common.*;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.data.DataExtensionFactory;
import ua.com.solidity.common.pgsql.SQLFlushMode;
import ua.com.solidity.common.pgsql.SQLTable;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;

public class PPDBTableWriter extends PPCustomDBWriter {
    private static final int DEFAULT_CACHE_SIZE = 67108864;
    private static final int DEFAULT_ROW_CACHE_SIZE = 1048576;
    private static final String NODE = "node";
    private static final String EXTENSION = "ext";
    private static final String PARAMS = "params";

    @AllArgsConstructor
    private static class Data {
        Input input;
        Input extension;
        SQLTable table;
        long objectCount;
        long errorCount;
        StatusChanger changer;
        ErrorReportLogger errorLogger;

        final boolean isValid() {
            return input != null && table != null;
        }
        public final boolean handleError(ErrorReport report) {
            if (errorLogger != null) {
                errorLogger.logError(report);
            }
            ++errorCount;
            return true;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class InternalParams {
        private String table = null;
        private ObjectNode mapping = null;
        private boolean singleTran = false;
        private int cacheSize = DEFAULT_CACHE_SIZE;
        private int rowCacheSize = DEFAULT_ROW_CACHE_SIZE;
        private String mode = "statement";
    }

    @Override
    public Class<?> getOutputClass() {
        return DataBatch.class;
    }

    @Override
    public Class<?> getInputClass() {
        return DataBatch.class;
    }

    @Override
    protected String getTableName(Item item) {
        return item.getInternalData(Data.class).table.tableName;
    }

    private SQLTable initSQLTable(Item item, String tableName, JsonNode mapping, boolean singleTransaction, SQLFlushMode mode,
                                  int cacheSize, int rowCacheSize) {
        if (tableName != null) {
            OutputCache cache = getOutputCache(item);
            if (cache == null) {
                item.terminate();
            } else {
                return SQLTable.create(tableName, mode, mapping, singleTransaction, cacheSize, rowCacheSize);
            }
        }
        return null;
    }

    private SQLTable initializeTable(Item item) {
        InternalParams params = item.getLocalData(PARAMS, InternalParams.class);
        if (params != null) {
            return initSQLTable(item, params.table, params.mapping, params.singleTran, SQLFlushMode.parse(params.mode),
                    params.cacheSize, params.rowCacheSize);
        }
        return null;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        super.initialize(item, node);
        item.setLocalData(NODE, node);
        item.mapInputs(EXTENSION, DataExtensionFactory.class);
        InternalParams params = Utils.jsonToValue(node, InternalParams.class);
        item.setLocalData(PARAMS, params);
        if (params != null) {
            item.setLocalData(GROUP, params.table);
        }
    }

    @Override
    protected void beforePipelineExecution(Item item) {
        super.beforePipelineExecution(item);
        if (item.terminated()) return;
        SQLTable table = initializeTable(item);
        if (table != null) {
            Data data = new Data(item.getInput(INPUT, 0), item.getInput(EXTENSION, 0), table, 0, 0, null,
                    item.getPipelineParam("logger", ErrorReportLogger.class));
            item.setInternalData(data);
            if (!data.isValid()) {
                item.terminate();
            }
        } else item.terminate();
    }

    @Override
    protected void beforeOutput(Item item, OutputCache cache) {
        Data data = item.getInternalData(Data.class);
        data.changer = new StatusChanger(data.table.tableName, "IMPORTER");
        data.changer.newStage("", "", 0);
        DataExtensionFactory factory = data.extension.getValue(DataExtensionFactory.class);
        if (!data.table.prepareTable(factory, cache, data::handleError)) {
            item.terminate();
        }
    }

    @Override
    protected void afterOutput(Item item, OutputCache cache) {
        Data data = item.getInternalData(Data.class);
        data.table.close();
        if (data.errorLogger != null) {
            data.errorLogger.finish();
        }

        OutputStats.Group group = cache.getGroup();

        data.changer.complete(String.format("Completed (%d total rows, %d parse errors, %d bad objects, %d rows inserted).",
                group.getTotalRowCount(), group.getParseErrorCount(), group.getBadObjectCount(),
                group.getInsertCount()));
    }

    @Override
    protected void flushObjects(Item item, OutputCache cache) {
        SQLTable table = item.getInternalData(Data.class).table;
        table.handleBatch();
    }

    @Override
    protected void changeStatus(Item item, OutputCache cache) {
        Data data = item.getInternalData(Data.class);
        OutputStats.Group group = cache.getGroup();
        data.changer.setProcessedVolume(group.getTotalRowCount());
        data.changer.setStatus(String.format("importing (%d total rows, %d parse errors, %d bad objects, %d rows inserted).",
                data.changer.getProcessedVolume(), group.getParseErrorCount(), group.getBadObjectCount(), group.getInsertCount()));
    }
}
