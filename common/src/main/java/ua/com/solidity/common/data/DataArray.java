package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;

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

    public final DataExtension getExtension() {
        return parent == null ? null : parent.getExtension();
    }

    public abstract int getArrayLength();

    public abstract DataField getItem(int index);

    public Iterable<DataField> items() {
        return () -> new Iterator<>() {
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < getArrayLength();
            }

            @Override
            public DataField next() throws NoSuchElementException {
                if (index < getArrayLength()) {
                    return getItem(index++);
                } else throw new NoSuchElementException("DataArray.items");
            }
        };
    }

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
