package ua.com.solidity.common.parsers.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataFieldType;
import ua.com.solidity.common.data.DataHeader;
import ua.com.solidity.common.data.DataObject;

import java.util.List;

public class CSVDataObject extends DataObject {
    private static class CSVField extends DataField {
        private final String data;

        public CSVField(DataObject parent, String data) {
            super(parent);
            this.data = data;
        }

        @Override
        public DataFieldType internalGetType() {
            return DataFieldType.STRING;
        }

        @Override
        public String internalGetString() {
            return data;
        }
    }

    private final DataHeader header;
    private final CSVField[] data;
    private JsonNode node = null;

    private CSVField createField(String data) {
        return new CSVField(this, data);
    }

    private CSVDataObject(DataHeader header, List<String> data, long row, long col) {
        super(null, row, col, -1, -1);
        this.header = header;
        this.data = data.stream().map(this::createField).toArray(CSVField[]::new);
    }

    public static CSVDataObject create(DataHeader header, List<String> data, long row, long col) {
        return header == null || data == null ? null : new CSVDataObject(header, data, row, col);
    }

    @Override
    public DataField getField(String fieldName) {
        int index = header.getFieldIndex(fieldName);
        return index >= 0 && index < data.length ? data[index] : null;
    }

    @Override
    public JsonNode getNode() {
        if (node != null) return node;
        ObjectNode obj = Utils.getSortedMapper().createObjectNode();
        for (var entry: header.getFieldIndexes()) {
            CSVField field = data[entry.getValue()];
            obj.set(entry.getKey(), field == null ? null : JsonNodeFactory.instance.textNode(DataField.getString(field)));
        }
        node = obj;
        return node;
    }
}
