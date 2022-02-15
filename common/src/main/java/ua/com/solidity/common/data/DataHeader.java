package ua.com.solidity.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataHeader {
    private final Map<String, Integer> fieldNames;

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
