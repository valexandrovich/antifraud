package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.com.solidity.common.Utils;

public abstract class DataObject {

    @AllArgsConstructor
    @Getter
    public static class FieldEntry {
        private final String name;
        private final DataField field;
    }

    private DataExtension extension = null;
    private final DataObject parent;
    private final DataLocation location;
    private JsonNode node = null;

    protected DataObject(DataObject parent, long row, long col, long byteOffset, long charOffset) {
        this.parent = parent;
        location = new DataLocation(row, col, byteOffset, charOffset, 0);
    }

    public final DataObject getParent() {
        return parent;
    }

    public final DataLocation getLocation() {
        return location;
    }

    public final void setExtension(DataExtension value) {
        this.extension = value;
    }

    public final DataExtension getExtension() {
        return extension;
    }

    public abstract DataField getField(String fieldName);

    public abstract Iterable<FieldEntry> getFields();

    public JsonNode getNode() {
        if (node != null) return node;
        node = Utils.getSortedMapper().createObjectNode();
        for (var entry : getFields()) {
            ((ObjectNode) node).set(entry.getName(), DataField.getNode(entry.getField()));
        }
        return node;
    }
}
