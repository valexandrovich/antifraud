package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

public class PPDictionary extends Prototype {
    private static final String INPUT = "input";

    @Override
    public Class<?> getOutputClass() {
        return null;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        if (node != null && node.hasNonNull(INPUT)) {
            item.addInput(INPUT, node.get(INPUT).asText(), JsonNode.class);
        }
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
