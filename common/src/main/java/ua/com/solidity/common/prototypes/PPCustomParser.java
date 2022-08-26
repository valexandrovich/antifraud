package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import ua.com.solidity.common.*;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.data.DataExtensionFactory;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.InputStream;

public abstract class PPCustomParser extends Prototype {
    protected static final String STREAM = "stream";
    protected static final String EXTENSION = "ext";
    protected static final String LIMITS = "limits";

    @NoArgsConstructor
    @Getter
    @Setter
    private static class Limits {
        private long parse = -1;
        private int batchSize = 0;
        private int batchRows = 0;
        private int batchErrors = 0;
    }

    static class Data {
        final CustomParser parser;
        final DataBatch batch;
        long limit;
        Input stream;
        Input extensionFactory;
        Data(CustomParser parser, DataBatch batch, long limit, Input stream, Input extensionFactory) {
            this.parser = parser;
            this.batch = batch;
            this.limit = limit;
            this.stream = stream;
            this.extensionFactory = extensionFactory;
        }
    }

    @Override
    public Class<?> getOutputClass() {
        return DataBatch.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        item.mapInputs(STREAM, InputStream.class);
        item.mapInputs(EXTENSION, DataExtensionFactory.class);

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

        DataBatch batch = new DataBatch(limits.getBatchSize(), this::batchFlush);
        batch.setItem(item);
        item.setInternalData(new Data(createParser(node.get("format")),
                batch, limits.parse, item.getInput(STREAM, 0), item.getInput(EXTENSION, 0)));
    }

    protected abstract CustomParser createParser(JsonNode format);

    private void batchFlush(DataBatch batch) {
        batch.getItem().yieldResult(batch, batch.getErrorCount() > 0); // or false
        batch.clear();
    }

    @Override
    protected Object execute(@NonNull Item item) {
        Data data = item.getInternalData(Data.class);
        InputStream stream = data.stream.getValue(InputStream.class);
        if (stream == null) {
            item.terminate();
        } else {
            data.batch.setExtensionFactory(data.extensionFactory != null ?
                    data.extensionFactory.getValue(DataExtensionFactory.class) : null);

            long count = 0;
            item.yieldBegin();
            if (data.parser.open(stream)) {
                while (data.parser.hasData() && (data.limit < 0 || count < data.limit)) {
                    if (data.parser.isErrorReporting()) {
                        data.batch.put(data.parser.getErrorReport());
                    } else {
                        DataObject obj = data.parser.dataObject();
                        data.batch.put(obj);
                    }
                    ++count;
                    data.parser.next();
                }
                data.batch.flush();
            }
        }

        return null;
    }

    @Override
    protected void close(Item item) {
        Data data = item.getInternalData(Data.class);
        if (data != null) {
            data.parser.close();
        }
    }
}
