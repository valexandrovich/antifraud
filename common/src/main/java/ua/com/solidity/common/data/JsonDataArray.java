package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonDataArray extends DataArray {
    private final ArrayNode node;

    private JsonDataArray(DataObject parent, ArrayNode node) {
        super(parent);
        this.node = node;
    }

    public static JsonDataArray create(DataObject parent, ArrayNode node) {
        return parent == null || node == null ? null : new JsonDataArray(parent, node);
    }

    @Override
    public int getArrayLength() {
        return node.size();
    }

    @Override
    public DataField getItem(int index) {
        return index < 0 || index >= node.size() ? null : JsonDataField.create(getParent(), node.get(index));
    }

    @Override
    public void enumerate(DataArrayItemHandler handler) {
        if (handler != null) {
            int size = node.size();
            DataObject p = getParent();
            for (int i = 0; i < size; ++i) {
                handler.handle(this, i, JsonDataField.create(p, node.get(i)));
            }
        }
    }
}
