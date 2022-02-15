package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
public abstract class DataObject {
    private DataExtension extension = null;
    private final DataObject parent;

    protected DataObject(DataObject parent) {
        this.parent = parent;
    }

    public final DataObject getParent() {
        return parent;
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
