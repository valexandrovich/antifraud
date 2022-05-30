package ua.com.solidity.common.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DataHeader {
    private final Map<String, Integer> fieldNames;

    public Iterable<DataObject.FieldEntry> fieldsIterator(DataField[] fields){
        return () -> new Iterator<>() {
            final Iterator<Map.Entry<String, Integer>> iterator = getFieldIndexes().iterator();
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public DataObject.FieldEntry next() {
                Map.Entry<String, Integer> item = iterator.next();
                if (item == null) return null;
                int index = item.getValue();
                DataField field = null;
                if (index >= 0 && index < fields.length) {
                    field = fields[index];
                }

                return new DataObject.FieldEntry(item.getKey(), field);
            }
        };
    }

    public DataHeader(Map<String, Integer> fieldNames) {
        this.fieldNames = new HashMap<>(fieldNames);
    }

    public final int getFieldIndex(String name) {
        return fieldNames.getOrDefault(name, -1);
    }

    public final Set<Map.Entry<String, Integer>> getFieldIndexes() {
        return fieldNames.entrySet();
    }
}
