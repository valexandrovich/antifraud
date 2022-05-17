package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class DataObject {
    private DataExtension extension = null;
    private final DataObject parent;
    private final DataLocation location;

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

    public abstract JsonNode getNode();

}
