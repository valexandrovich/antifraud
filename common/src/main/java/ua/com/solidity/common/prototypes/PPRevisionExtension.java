package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.data.DataExtensionFactory;
import ua.com.solidity.common.pgsql.RevisionExtensionFactory;
import ua.com.solidity.common.pgsql.RevisionExtensionType;
import ua.com.solidity.pipeline.Item;

public class PPRevisionExtension extends PPCustomExtension {
    private static final String DATA = "data";
    private static final String TYPE = "type";

    @Override
    protected void initialize(Item item, JsonNode node) {
        super.initialize(item, node);
        String typeName = node != null && node.hasNonNull(TYPE) ? node.get(TYPE).asText("root") : "root";
        RevisionExtensionType type = RevisionExtensionType.parse(typeName);
        item.setLocalData(TYPE, type);
    }

    @Override
    protected DataExtensionFactory createExtensionFactory(Item item) {
        ImporterMessageData importerMessageData = item.getPipelineParam(DATA, ImporterMessageData.class);
        RevisionExtensionType type = item.getLocalData(TYPE, RevisionExtensionType.class);
        return new RevisionExtensionFactory(importerMessageData.getImportRevisionId(), type);
    }
}
