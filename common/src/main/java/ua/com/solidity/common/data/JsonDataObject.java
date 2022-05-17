package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDataObject extends DataObject {
    private final ObjectNode node;

    private JsonDataObject(DataObject parent, ObjectNode node, long row, long col, long byteOffset, long charOffset) {
        super(parent, row, col, byteOffset, charOffset);
        this.node = node;
    }

    public static JsonDataObject create(DataObject parent, JsonNode node, long row, long col, long byteOffset, long charOffset) {
        return node == null || !node.isObject() ? null : new JsonDataObject(parent, (ObjectNode) node, row, col, byteOffset, charOffset);
    }

    public static JsonDataObject create(DataObject parent, JsonNode node, DataLocation location) {
        return create(parent, node, location.getRow(), location.getCol(), location.getByteOffset(), location.getCharOffset());
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
