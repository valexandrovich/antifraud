package ua.com.solidity.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j

public abstract class Prototype {
    public final void createInstance(Pipeline pipeline, String name, Map<String, List<String>> inputs, JsonNode node) {
        Item.createItem(this, pipeline, name, inputs, node);
    }

    public abstract Class<?> getOutputClass();

    protected abstract void initialize(Item item, JsonNode node);

    protected void beforePipelineExecution(Item item) {
        // nothing
    }

    protected void open(Item item) {
        // nothing
    }

    protected abstract Object execute(@NonNull Item item);

    protected abstract void close(Item item);
}
