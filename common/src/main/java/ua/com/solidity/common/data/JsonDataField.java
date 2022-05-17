package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonDataField extends DataField {
    private final JsonNode node;
    private DataObject obj = null;
    private DataArray arr = null;
    private boolean typeAssigned = false;
    private DataFieldType internalType;

    public static DataField create(DataObject parent, JsonNode node) {
        return parent == null || node == null ? null : new JsonDataField(parent, node);
    }

    protected JsonDataField(DataObject parent, JsonNode node) {
        super(parent);
        this.node = node;
    }

    @Override
    protected DataFieldType internalGetType() {
        if (typeAssigned) return internalType;
        switch (node.getNodeType()) {
            case NULL:
                internalType = DataFieldType.NULL;
                break;
            case NUMBER:
                internalType = DataFieldType.NUMBER;
                break;
            case BOOLEAN:
                internalType = DataFieldType.BOOLEAN;
                break;
            case ARRAY:
                internalType = DataFieldType.ARRAY;
                break;
            case OBJECT:
                internalType = DataFieldType.OBJECT;
                break;
            default:
                internalType = DataFieldType.STRING;
        }

        typeAssigned = true;
        return internalType;
    }

    @Override
    protected String internalGetString() {
        return internalGetType() == DataFieldType.STRING ? node.asText() : null;
    }

    @Override
    protected Number internalGetNumber() {
        if (internalGetType() == DataFieldType.NUMBER) {
            return node.isFloatingPointNumber() ? node.asDouble() : node.asLong();
        }
        return null;
    }

    @Override
    protected Boolean internalGetBoolean() {
        return internalGetType() == DataFieldType.BOOLEAN ? node.asBoolean() : null;
    }

    @Override
    protected DataObject internalGetDataObject() {
        if (obj == null && internalGetType() == DataFieldType.OBJECT) {
            obj = JsonDataObject.create(parent, node, parent.getLocation());
        }
        return obj;
    }

    @Override
    protected DataArray internalGetDataArray() {
        if (arr == null && internalGetType() == DataFieldType.ARRAY) {
            arr = JsonDataArray.create(parent, (ArrayNode) node);
        }
        return arr;
    }
}
