package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonDataField extends DataField {
    private JsonNode node;
    private final Object index;
    private DataObject obj = null;
    private DataArray arr = null;
    private boolean typeAssigned = false;
    private DataFieldType internalType;

    public static DataField create(DataObject parent, JsonNode node, Object index) {
        return parent == null || node == null  || index == null ? null : new JsonDataField(parent, node, index);
    }

    protected JsonDataField(DataObject parent, JsonNode node, Object index) {
        super(parent);
        this.node = node;
        this.index = index;
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

    private boolean internalPut(JsonNode parentNode, JsonNode value, DataObject newObj, DataArray newArr) {
        obj = newObj;
        arr = newArr;
        if (parentNode.isObject()) {
            ((ObjectNode) parentNode).set((String) index, node = value);
        } else if (parentNode.isArray()) {
            ((ArrayNode) parentNode).set(((Number) index).intValue(), node = value);
        } else return false;
        return true;
    }

    private boolean internalPut(JsonNode parentNode, JsonNode value) {
        return internalPut(parentNode, value, null, null);
    }

    private boolean internalPutNumber(JsonNode parentNode, Number value) {
        if (value instanceof Long) {
            internalPut(parentNode, JsonNodeFactory.instance.numberNode(value.longValue()));
            return true;
        }

        if (value instanceof Integer) {
            internalPut(parentNode, JsonNodeFactory.instance.numberNode(value.intValue()));
            return true;
        }

        if (value instanceof Short) {
            internalPut(parentNode, JsonNodeFactory.instance.numberNode(value.shortValue()));
            return true;
        }

        if (value instanceof Byte) {
            internalPut(parentNode, JsonNodeFactory.instance.numberNode(value.byteValue()));
            return true;
        }

        if (value instanceof Float) {
            internalPut(parentNode, JsonNodeFactory.instance.numberNode(value.floatValue()));
            return true;
        }

        if (value instanceof Double) {
            internalPut(parentNode, JsonNodeFactory.instance.numberNode(value.doubleValue()));
            return true;
        }

        if (value instanceof BigInteger) {
            internalPut(parentNode, JsonNodeFactory.instance.numberNode((BigInteger) value));
            return true;
        }

        if (value instanceof BigDecimal) {
            internalPut(parentNode, JsonNodeFactory.instance.numberNode((BigDecimal) value));
            return true;
        }

        return false;
    }

    @Override
    protected void internalSetValue(DataFieldType type, Object value) {
        JsonNode parentNode = parent.getNode();
        if (parentNode == null) return;
        switch (type) {
            case NULL:
                if (internalPut(parentNode, JsonNodeFactory.instance.nullNode())) return;
                break;

            case STRING:
                if (internalPut(parentNode, JsonNodeFactory.instance.textNode((String) value))) return;
                break;

            case NUMBER:
                assert value instanceof Number;
                if (internalPutNumber(parentNode, (Number) value)) return;
                break;

            case BOOLEAN:
                if (value instanceof Boolean &&
                        internalPut(parentNode, JsonNodeFactory.instance.booleanNode((Boolean) value))) return;
                break;

            case OBJECT:
                DataObject dataObject = value instanceof DataObject ? (DataObject) value : null;
                if (dataObject != null && internalPut(parentNode, dataObject.getNode(), dataObject, null)) return;
                break;

            case ARRAY:
                DataArray dataArray = value instanceof DataArray ? (DataArray) value : null;
                if (dataArray != null && internalPut(parentNode, dataArray.getNode(), null, dataArray)) return;
                break;
        }
        super.internalSetValue(type, value);
    }
}
