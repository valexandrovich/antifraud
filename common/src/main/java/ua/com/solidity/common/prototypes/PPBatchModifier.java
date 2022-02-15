package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.data.DataModifier;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.data.JsonDataObject;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.util.List;

@Slf4j
@SuppressWarnings("unused")
public class PPBatchModifier extends Prototype {
    private static final String INPUT = "input";
    private static final String MODIFIERS = "modifiers";

    private static class Data {
        Input batchInput;
        DataModifier[] modifiers;
        public Data(Input batchInput, List<Input> modifiers) {
            this.batchInput = batchInput;
            if (modifiers == null || modifiers.isEmpty()) {
                this.modifiers = new DataModifier[0];
            } else {
                this.modifiers = modifiers.stream().
                        filter(input->input.getValue(DataModifier.class) != null).
                        map(input->input.getValue(DataModifier.class)).toArray(DataModifier[]::new);
            }
        }

        public final boolean isValid() {
            return batchInput != null;
        }

        public final DataBatch modify() {
            DataBatch batch = batchInput.getValue(DataBatch.class);
            if (batch != null) {
                int batchSize = batch.getObjectCount();
                for (int i = 0; i < batchSize; ++i) {
                    DataObject obj = batch.get(i);
                    if (obj != null) {
                        JsonNode node = obj.getNode();
                        for (DataModifier modifier : modifiers) {
                            modifier.handle(node);
                        }
                        batch.replace(i, JsonDataObject.create(obj.getParent(), node));
                    }
                }
            }
            return batch;
        }
    }

    @Override
    public Class<?> getOutputClass() {
        return DataBatch.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        item.mapInputs(INPUT, DataBatch.class);
        item.mapInputs(MODIFIERS, DataModifier.class);
    }

    @Override
    protected void beforePipelineExecution(Item item) {
        Data data = new Data(item.getInput(INPUT, 0), item.getInputs(MODIFIERS));
        if (!data.isValid()) {
            log.error("BatchModifier invalid inputs.");
            item.terminate();
        }
        item.setInternalData(data);
    }

    @Override
    protected Object execute(@NonNull Item item) {
        Data data = item.getInternalData(Data.class);
        if (data != null) {
            if (data.batchInput.isIterator()) {
                item.yieldBegin();
                if (data.batchInput.hasData()) {
                    item.yieldResult(data.modify(), false);
                    item.stayUncompleted();
                }
            } else {
                return data.modify();
            }
        }
        return null;
    }

    @Override
    protected void close(Item item) {
        // nothing
    }
}
