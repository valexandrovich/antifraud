package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.data.DataExtensionFactory;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.InputStream;

public abstract class PPCustomParser extends Prototype {
    protected static final String STREAM = "stream";
    protected static final String EXTENSION = "ext";
    protected static final String PARSER = "parser";
    protected static final String BATCH = "batch";
    protected static final String LIMITS = "limits";
    protected static final String PARSE_LIMIT = "parse_limit";

    @NoArgsConstructor
    @Getter
    @Setter
    private static class Limits {
        private long parse = -1;
        private int batchSize = 0;
        private int batchRows = 0;
        private int batchErrors = 0;
    }

    @Override
    public Class<?> getOutputClass() {
        return DataBatch.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        item.mapInputs(STREAM, InputStream.class);
        item.mapInputs(EXTENSION, DataExtensionFactory.class);
        item.setLocalData(PARSER, createParser(node.get("format")));
        Limits limits = null;
        if (node.hasNonNull(LIMITS)) {
            JsonNode limitsNode = node.get(LIMITS);
            if (limitsNode.isObject()) {
                limits = Utils.jsonToValue(limitsNode, Limits.class);
            }
        }
        if (limits == null) {
            limits = new Limits();
        }
        item.setLocalData(BATCH, new DataBatch(limits.getBatchSize(), limits.getBatchRows(), limits.getBatchErrors(), item, this::batchFlush));
        item.setLocalData(PARSE_LIMIT, limits.parse);
    }

    protected abstract CustomParser createParser(JsonNode format);

    private void batchFlush(DataBatch batch) {
        batch.getItem().yieldResult(batch, batch.getErrorCount() > 0); // or false
        batch.clear();
    }

    @Override
    protected Object execute(@NonNull Item item) {
        CustomParser parser = item.getLocalData(PARSER, CustomParser.class);
        DataBatch batch = item.getLocalData(BATCH, DataBatch.class);
        InputStream stream = item.getInputValue(STREAM, 0, InputStream.class);
        DataExtensionFactory extensionFactory = item.getInputValue(EXTENSION, 0, DataExtensionFactory.class);
        long limit = item.getLocalData(PARSE_LIMIT, Long.class);
        batch.setExtensionFactory(extensionFactory);

        long count = 0;
        item.yieldBegin();
        if (parser.open(stream)) {
            while (parser.hasData() && (limit < 0 || count < limit)) {
                if (parser.isErrorReporting()) {
                    batch.put(parser.getErrorReport());
                } else {
                    DataObject obj = parser.dataObject();
                    if (extensionFactory != null && obj != null) {
                        extensionFactory.handle(obj);
                    }
                    batch.put(obj);
                }
                ++count;
                parser.next();
            }
            batch.flush();
        }

        return null;
    }

    @Override
    protected void close(Item item) {
        item.getLocalData(PARSER, CustomParser.class).close();
    }
}
