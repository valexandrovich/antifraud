package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import ua.com.solidity.common.data.DataExtensionFactory;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

public abstract class PPCustomExtension extends Prototype {
    @Override
    public Class<?> getOutputClass() {
        return DataExtensionFactory.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        // nothing
    }

    protected abstract DataExtensionFactory createExtensionFactory(Item item);

    @Override
    protected void beforePipelineExecution(Item item) {
        item.setInternalData(createExtensionFactory(item));
    }

    @Override
    protected Object execute(@NonNull Item item) {
        return item.getInternalData(DataExtensionFactory.class);
    }

    @Override
    protected void close(Item item) {
        // nothing
    }
}
