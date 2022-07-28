package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

public class PPObjectExtractor extends Prototype {

    @Override
    public Class<?> getOutputClass() {
        return DataBatch.class;
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

    }
}
