package ua.com.solidity.common.parsers.csv;

import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataFieldType;
import ua.com.solidity.common.data.DataHeader;
import ua.com.solidity.common.data.DataObject;

import java.util.*;

public class CSVDataObject extends DataObject {

    private static class CSVField extends DataField {
        private String data;

        public CSVField(DataObject parent, String data) {
            super(parent);
            this.data = data;
        }

        @Override
        public DataFieldType internalGetType() {
            return data == null ? DataFieldType.NULL : DataFieldType.STRING;
        }

        @Override
        public String internalGetString() {
            return data == null ? "" : data;
        }

        @Override
        protected void internalSetValue(DataFieldType type, Object value) {
            switch (type) {
                case NULL:
                    data = null;
                    break;
                case STRING:
                case NUMBER:
                case BOOLEAN:
                    data = value.toString();
                    break;
                default:
                    super.internalSetValue(type, value);
            }
        }
    }

    private final DataHeader header;
    private final CSVField[] data;

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
    public Iterable<FieldEntry> getFields() {
        return header.fieldsIterator(data);
    }
}
