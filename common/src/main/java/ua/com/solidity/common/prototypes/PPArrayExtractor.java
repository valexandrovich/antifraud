package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.NonNull;
import ua.com.solidity.common.data.*;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;


@CustomLog
public class PPArrayExtractor extends Prototype {
    public static final String INPUT = "input";
    public static final String EXTENSION = "ext";
    public static final String PATH = "path";
    public static final String BATCH_SIZE = "batch_object_count";
    public static final String ARRAY_NODE = "isArray";

    private static class Data {
        Input input;
        Input extension;
        DataExtensionFactory ext;
        boolean extAssigned = false;
        boolean isArray;
        String path;
        DataBatch batch;
        DataBatch inputBatch;

        public Data(String path, boolean isArray, int batchSize, Item item, DataBatch.FlushHandler handler) {
            this.path = path;
            this.isArray = isArray;
            this.batch = new DataBatch(0, batchSize, 1, item, handler);
        }

        public final boolean setInputs(Input input, Input extension) {
            if (input == null) return false;
            this.input = input;
            this.extension = extension;
            return true;
        }

        public final void updateBatch() {
            inputBatch = input.getValue(DataBatch.class);
        }

        public final void applyExtension(DataObject obj) {
            if (!extAssigned && extension != null) {
                ext = extension.getValue(DataExtensionFactory.class);
                extAssigned = true;
            }
            if (ext != null) {
                ext.handle(obj);
            }
        }
    }

    @Override
    public Class<?> getOutputClass() {
        return DataBatch.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        int batchSize = 0;
        String path = null;

        item.mapInputs(INPUT, DataBatch.class);
        if (node != null) {
            if (node.hasNonNull(PATH)) {
                path = node.get(PATH).asText();
            }

            if (node.hasNonNull(BATCH_SIZE)) {
                batchSize = node.get(BATCH_SIZE).asInt(0);
            }

            if (path == null) {
                log.error("RevisionAssignerList path property is null.");
                item.terminate();
            } else {
                item.setInternalData(new Data(path, !node.hasNonNull(ARRAY_NODE) || node.asBoolean(true), batchSize, item, this::doFlush));
            }
        } else item.terminate();
    }

    @Override
    protected void beforePipelineExecution(Item item) {
        Data data = item.getInternalData(Data.class);
        if (data == null || !data.setInputs(item.getInput("INPUT", 0), item.getInput(EXTENSION, 0))) {
            log.error("Input not assigned for ArrayExtractor.");
            item.terminate();
        }
    }

    private void doFlush(DataBatch batch) {
        batch.getItem().yieldResult(batch, false);
    }

    private void doHandleArrayItem(Data data, DataField field) {
        DataObject obj = DataField.getObject(field);
        if (obj != null) {
            data.applyExtension(obj);
            data.batch.put(obj);
        }
    }

    private DataObject doHandleObject(Data data, DataObject obj) {
        if (obj != null) {
            DataArray dataArray = DataField.getArray(obj.getField(data.path));
            if (dataArray != null) {
                dataArray.enumerate((a, i, f) -> doHandleArrayItem(data, f));
            }
        }
        return obj;
    }

    private void handleBatch(Data data) {
        if (data.inputBatch != null) {
            data.inputBatch.modify(obj->doHandleObject(data, obj));
        }
    }

    @Override
    protected Object execute(@NonNull Item item) {
        Data data = item.getInternalData(Data.class);
        data.updateBatch();
        if (data.input.isIterator()) {
            if (data.input.isBOF()) {
                item.yieldBegin();
            }
            if (data.input.hasData()) {
                handleBatch(data);
                item.stayUncompleted();
            }
        } else {
            item.yieldBegin();
            handleBatch(data);
        }
        return null;
    }

    @Override
    protected void close(Item item) {
        Data data = item.getInternalData(Data.class);
        if (data != null) {
            data.batch.flush();
        }
        item.setInternalData(null);
    }
}
