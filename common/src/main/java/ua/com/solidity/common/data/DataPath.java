package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class DataPath {
    private final List<String> names = new ArrayList<>();

    @SuppressWarnings("unused")
    private DataPath(String path) {
        initializeByString(path);
    }

    @SuppressWarnings("unused")
    public DataPath(String ... items) {
        initializeByArray(items);
    }

    @SuppressWarnings("unused")
    public DataPath(Collection<String> items) {
        names.addAll(items);
    }

    public DataPath(JsonNode node) {
        if (node != null) {
            if (node.isTextual()) {
                initializeByString(node.asText());
            } else if (node.isArray()) {
                for (var item: node) {
                    names.add(item.asText());
                }
            }
        }
    }

    private void initializeByString(String path) {
        if (path != null && !path.isBlank()) {
            initializeByArray(path.split("\\."));
        }
    }

    private void initializeByArray(String[] items) {
        for (var item : items) {
            names.add(item.trim());
        }
    }

    public final DataField getField(DataObject obj) {
        DataField top = null;
        for (var name: names) {
            if (top != null) {
                if (DataField.isObject(top)) {
                    obj = DataField.getObject(top);
                } else return null;
            }
            if (obj == null) return null;
            top = obj.getField(name);
        }
        return top;
    }
}
