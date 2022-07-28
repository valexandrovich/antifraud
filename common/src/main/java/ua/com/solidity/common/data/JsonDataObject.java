package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

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
        return res == null || res.isMissingNode() ? null : JsonDataField.create(this, res, fieldName);
    }

    @Override
    public Iterable<FieldEntry> getFields() {
        JsonDataObject object = this;
        return () -> new Iterator<>() {
            final Iterator<Map.Entry<String, JsonNode>> iterator = node == null ? null : node.fields();
            @Override
            public boolean hasNext() {
                return iterator != null && iterator.hasNext();
            }

            @Override
            public FieldEntry next() {
                Map.Entry<String, JsonNode> item = iterator == null ? null : iterator.next();
                if (item == null) return null;
                return new FieldEntry(item.getKey(), JsonDataField.create(object, item.getValue(), item.getKey()));
            }
        };
    }

    @Override
    public JsonNode getNode() {
        return node;
    }
}
