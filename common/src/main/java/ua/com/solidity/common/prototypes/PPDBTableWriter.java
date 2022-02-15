package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.pgsql.SQLFlushMode;
import ua.com.solidity.common.pgsql.SQLTable;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;

public class PPDBTableWriter extends PPCustomDBWriter {
    private static final int DEFAULT_CACHE_SIZE = 67108864;
    private static final int DEFAULT_ROW_CACHE_SIZE = 1048576;
    private static final String NODE = "node";

    @AllArgsConstructor
    private static class Data {
        Input input;
        SQLTable table;

        final boolean isValid() {
            return input != null && table != null;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class InternalParams {
        private String table = null;
        private ObjectNode mapping = null;
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

    private SQLTable initSQLTable(Item item, String tableName, JsonNode mapping, SQLFlushMode mode,
                                  int cacheSize, int rowCacheSize) {
        if (tableName != null && mapping != null) {
            OutputCache cache = getOutputCache(item);
            if (cache == null) {
                item.terminate();
            } else {
                return SQLTable.create(tableName, mode, mapping, cacheSize, rowCacheSize);
            }
        }
        return null;
    }

    private SQLTable initializeTable(Item item, JsonNode node) {
        String tableName = null;
        JsonNode mapping = null;
        int cacheSize = DEFAULT_CACHE_SIZE;
        int rowCacheSize = DEFAULT_ROW_CACHE_SIZE;
        SQLFlushMode mode = SQLFlushMode.PREPARED_STATEMENT_BATCH;

        if (node != null) {
            InternalParams params = Utils.jsonToValue(node, InternalParams.class);
            tableName = params.table;
            mapping = params.mapping;
            cacheSize = params.cacheSize;
            rowCacheSize = params.rowCacheSize;
            mode = SQLFlushMode.parse(params.mode);
        }

        return initSQLTable(item, tableName, mapping,  mode, cacheSize, rowCacheSize);
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        super.initialize(item, node);
        item.setLocalData(NODE, node);
    }

    @Override
    protected void beforePipelineExecution(Item item) {
        super.beforePipelineExecution(item);
        SQLTable table = initializeTable(item, item.getLocalData(NODE, JsonNode.class));
        if (table != null) {
            Data data = new Data(item.getInput(INPUT, 0), table);
            item.setInternalData(data);
            if (!data.isValid()) {
                item.terminate();
            }
        } else item.terminate();
    }

    @Override
    protected void beforeOutput(Item item, OutputCache cache) {
        // nothing
    }

    @Override
    protected void afterOutput(Item item, OutputCache cache) {
        Data data = item.getInternalData(Data.class);
        data.table.close();
    }

    @Override
    protected int flushObjects(Item item, OutputCache cache) {
        return item.getInternalData(Data.class).table.doOutput(cache);
    }

    @Override
    protected int flushErrors(Item item, OutputCache cache) {
        // nothing yet
        return 0;
    }
}
