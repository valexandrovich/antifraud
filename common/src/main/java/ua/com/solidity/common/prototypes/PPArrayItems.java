package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

/******************************************
*
* Extracts array of items from jsonNode
*
*******************************************/

public class PPArrayItems extends Prototype {
    @Override
    public Class<?> getOutputClass() {
        return null;
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
        // nothing
    }
}
