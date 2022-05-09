package ua.com.solidity.importer.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

@SuppressWarnings("unused")
public class MappingModifierCollectorPrototype extends Prototype {
    @Override
    public Class<?> getOutputClass() {
        return JsonNode.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        // nothing yet
    }

    @Override
    protected Object execute(@NonNull Item item) {
        return null;
    }

    @Override
    protected void close(Item item) {
        // nothing yet
    }
}
