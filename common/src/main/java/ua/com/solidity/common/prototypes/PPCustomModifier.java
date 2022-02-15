package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import ua.com.solidity.common.data.DataModifier;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

@SuppressWarnings("unused")
public abstract class PPCustomModifier extends Prototype {
    @Override
    public Class<?> getOutputClass() {
        return DataModifier.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        // nothing
    }

    protected abstract DataModifier createModifier(Item item);

    @Override
    protected void beforePipelineExecution(Item item) {
        item.setInternalData(createModifier(item));
    }

    @Override
    protected Object execute(@NonNull Item item) {
        return item.getInternalData(DataModifier.class);
    }

    @Override
    protected void close(Item item) {
        // nothing
    }
}
