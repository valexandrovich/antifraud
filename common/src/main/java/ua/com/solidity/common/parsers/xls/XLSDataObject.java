package ua.com.solidity.common.parsers.xls;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataFieldType;
import ua.com.solidity.common.data.DataHeader;
import ua.com.solidity.common.data.DataObject;
import java.util.stream.Stream;

public class XLSDataObject extends DataObject {
    private static class XLSField extends DataField {
        private final Object data;
        private final DataFieldType dataFieldType;

        public XLSField(DataObject parent, Object data) {
            super(parent);
            if (data instanceof String) {
                this.dataFieldType = DataFieldType.STRING;
            } else if (data instanceof Number) {
                this.dataFieldType = DataFieldType.NUMBER;
            } else if (data instanceof Boolean) {
                dataFieldType = DataFieldType.BOOLEAN;
            } else {
                dataFieldType = DataFieldType.NULL;
            }
            this.data = dataFieldType == DataFieldType.NULL ? null : data;
        }

        @Override
        public DataFieldType internalGetType() {
            return dataFieldType;
        }

        @Override
        public String internalGetString() {
            return dataFieldType == DataFieldType.STRING ? (String) data : null;
        }

        @Override
        public Number internalGetNumber() {
            return dataFieldType == DataFieldType.NUMBER ? (Number) data : null;
        }

        @Override
        public Boolean internalGetBoolean() {
            return dataFieldType == DataFieldType.BOOLEAN ? (Boolean) data : null;
        }
    }

    private final XLSField[] data;
    private final DataHeader header;
    private JsonNode node = null;

    private XLSField createField(Object data) {
        return new XLSField(this, data);
    }

    private XLSDataObject(DataHeader header, Object[] data) {
        super(null);
        this.header = header;
        this.data = Stream.of(data).map(this::createField).toArray(XLSField[]::new);
    }

    public static XLSDataObject create(DataHeader header, Object[] data) {
        return header == null || data == null ? null : new XLSDataObject(header, data);
    }

    @Override
    public DataField getField(String fieldName) {
        int index = header.getFieldIndex(fieldName);
        return index >= 0 && index < data.length ? data[index] : null;
    }

    @Override
    public JsonNode getNode() {
        if (node != null) return node;
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        for (var entry: header.getFieldIndexes()) {
            XLSField field = data[entry.getValue()];
            obj.set(entry.getKey(), field == null ? null : JsonNodeFactory.instance.textNode(DataField.getString(field)));
        }
        node = obj;
        return node;
    }
}
