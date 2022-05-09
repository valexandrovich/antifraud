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

    @AllArgsConstructor
    private static class Data {
        Input input;
        Input extension;
        SQLTable table;
        ErrorReportLogger errorLogger;

        final boolean isValid() {
            return input != null && table != null;
        }
        public final boolean handleError(ErrorReport report) {
            if (errorLogger != null) {
                errorLogger.logError(report);
            }
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
        return null;
    }

    @Override
    public Class<?> getInputClass() {
        return DataBatch.class;
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

    private SQLTable initializeTable(Item item, JsonNode node) {
        String tableName = null;
        JsonNode mapping = null;
        int cacheSize = DEFAULT_CACHE_SIZE;
        int rowCacheSize = DEFAULT_ROW_CACHE_SIZE;
        boolean singleTran = false;
        SQLFlushMode mode = SQLFlushMode.PREPARED_STATEMENT_BATCH;

        if (node != null) {
            InternalParams params = Utils.jsonToValue(node, InternalParams.class);
            tableName = params.table;
            mapping = params.mapping;
            cacheSize = params.cacheSize;
            rowCacheSize = params.rowCacheSize;
            singleTran = params.isSingleTran();
            mode = SQLFlushMode.parse(params.mode);
        }

        return initSQLTable(item, tableName, mapping, singleTran, mode, cacheSize, rowCacheSize);
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        super.initialize(item, node);
        item.setLocalData(NODE, node);
        item.mapInputs(EXTENSION, DataExtensionFactory.class);
    }

    @Override
    protected void beforePipelineExecution(Item item) {
        super.beforePipelineExecution(item);
        if (item.terminated()) return;
        SQLTable table = initializeTable(item, item.getLocalData(NODE, JsonNode.class));
        if (table != null) {
            Data data = new Data(item.getInput(INPUT, 0), item.getInput(EXTENSION, 0), table, item.getPipelineParam("logger", ErrorReportLogger.class));
            item.setInternalData(data);
            if (!data.isValid()) {
                item.terminate();
            }
        } else item.terminate();
    }

    @Override
    protected void beforeOutput(Item item, OutputCache cache) {
        Data data = item.getInternalData(Data.class);
        DataExtensionFactory factory = data.extension.getValue(DataExtensionFactory.class);
        if (!data.table.prepareTable(factory)) {
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
    }

    @Override
    protected int flushObjects(Item item, OutputCache cache) {
        return item.getInternalData(Data.class).table.doOutput(cache);
    }

    @Override
    protected int flushErrors(Item item, OutputCache cache) {
        return cache.getBatch().handleErrors(item.getInternalData(Data.class)::handleError);
    }
}
