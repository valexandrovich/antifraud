package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

@SuppressWarnings("unused")
public abstract class DataArray {
    public interface DataArrayItemHandler {
        void handle(DataArray array, int index, DataField field);
    }

    private final DataObject parent;

    protected DataArray(DataObject parent) {
        this.parent = parent;
    }

    public final DataObject getParent() {
        return parent;
    }

    public abstract int getArrayLength();

    public abstract DataField getItem(int index);

    public void enumerate(DataArrayItemHandler handler) {
        if (handler != null) {
            int size = getArrayLength();
            for (int i = 0; i < size; ++i) {
                handler.handle(this, i, getItem(i));
            }
        }
    }

    public JsonNode getNode() {
        ArrayNode res = JsonNodeFactory.instance.arrayNode();
        enumerate((p, i, v) -> res.add(DataField.getNode(v)));
        return res;
    }
}
