package ua.com.solidity.importer.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

public class MappingPrototype extends Prototype {
    @Override
    public Class<?> getOutputClass() {
        return null;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {

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