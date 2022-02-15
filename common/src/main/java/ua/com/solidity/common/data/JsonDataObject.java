package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDataObject extends DataObject {
    private final ObjectNode node;

    private JsonDataObject(DataObject parent, ObjectNode node) {
        super(parent);
        this.node = node;
    }

    public static JsonDataObject create(DataObject parent, JsonNode node) {
        return node == null || !node.isObject() ? null : new JsonDataObject(parent, (ObjectNode) node);
    }

    @Override
    public DataField getField(String fieldName) {
        JsonNode res = node.path(fieldName);
        return res == null || res.isMissingNode() ? null : JsonDataField.create(this, res);
    }

    @Override
    public JsonNode getNode() {
        return node;
    }
}
