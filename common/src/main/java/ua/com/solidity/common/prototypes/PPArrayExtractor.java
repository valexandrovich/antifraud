package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.NonNull;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.data.*;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

@CustomLog
public class PPArrayExtractor extends Prototype {
    public static int DEFAULT_BATCH_SIZE = 16384;
    public static final String INPUT = "input";
    public static final String EXTENSION = "ext";
    public static final String PATH = "path";
    public static final String BATCH_SIZE = "batch_size";

    private static class Data {
        Input input;
        Input extension;
        DataPath path;
        DataBatch batch;
        DataBatch inputBatch;

        public Data(JsonNode path, int batchSize, Item item, DataBatch.FlushHandler handler, Input input, Input extension) {
            this.path = new DataPath(path);
            this.batch = new DataBatch(batchSize, handler);
            this.batch.setItem(item);
            this.input = input;
            this.extension = extension;
        }

        public final void updateBatch() {
            inputBatch = input.getValue(DataBatch.class);
            batch.setExtensionFactory(extension.getValue(DataExtensionFactory.class));
            if (inputBatch != null) {
                batch.setPortion(inputBatch.getPortion());
            }
        }
    }

    @Override
    public Class<?> getOutputClass() {
        return DataBatch.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        int batchSize = DEFAULT_BATCH_SIZE;
        JsonNode path;

        item.mapInputs(INPUT, DataBatch.class);
        item.mapInputs(EXTENSION, DataExtensionFactory.class);

        if (node != null) {
            path = node.get(PATH);

            if (node.hasNonNull(BATCH_SIZE)) {
                batchSize = node.get(BATCH_SIZE).asInt(0);
            }

            if (path == null || path.isNull()) {
                log.error("ArrayExtractor path property is null.");
                item.terminate();
            } else {
                item.setInternalData(new Data(path, batchSize, item, this::doFlush,
                        item.getInput(INPUT, 0), item.getInput(EXTENSION, 0)));

            }
        } else item.terminate();
    }

    private void doFlush(DataBatch batch) {
        batch.getItem().yieldResult(batch, false);
        batch.clear();
    }

    private void doHandleArrayItem(Data data, DataField field) {
        DataObject obj = new ArrayItemObject(field);
        data.batch.put(obj);
    }

    private ErrorResult doHandleObject(Data data, DataObject obj) {
        if (obj != null) {
            DataField field = data.path.getField(obj);
            if (field == null) return null;
            if (DataField.isArray(field)) {
                DataArray dataArray = DataField.getArray(field);
                if (dataArray != null) {
                    dataArray.enumerate((a, i, f) -> this.doHandleArrayItem(data, f));
                }
            } else {
                doHandleArrayItem(data, field);
            }
        }
        return null;
    }

    private boolean doHandleError(ErrorReport report) {
        return true;
    }

    private void handleBatch(Data data) {
        if (data.inputBatch != null && !data.inputBatch.isEmpty()) {
            data.inputBatch.handle((obj)->doHandleObject(data, obj), this::doHandleError);
            data.batch.flush();
        }
    }

    @Override
    protected Object execute(@NonNull Item item) {
        Data data = item.getInternalData(Data.class);
        data.updateBatch();
        Input input = data.input;

        if (input.isBOF()) {
            item.yieldBegin();
        }

        if (!item.terminated()) {
            if (input.hasData()) {
                handleBatch(data);
                item.stayUncompleted();
            }
        }
        return null;
    }

    @Override
    protected void close(Item item) {
        // nothing yet
    }
}
