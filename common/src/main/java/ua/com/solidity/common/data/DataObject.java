package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.common.Utils;

public abstract class DataObject {

    public interface ErrorHandler {
        void handle(ErrorResult oldErrorResult, ErrorResult newErrorResult);
    }

    @AllArgsConstructor
    @Getter
    public static class FieldEntry {
        private final String name;
        private final DataField field;
    }

    @Getter
    @Setter
    private DataExtension extension = null;
    private final DataObject parent;
    private final DataLocation location;
    private JsonNode node = null;
    private ErrorHandler errorHandler = null;
    @Getter
    private ErrorResult errorResult = null;

    protected DataObject(DataObject parent, DataLocation location) {
        this.parent = parent;
        this.location = location;
    }

    protected DataObject(DataObject parent, long row, long col, long byteOffset, long charOffset) {
        this.parent = parent;
        location = new DataLocation(row, col, byteOffset, charOffset, 0);
    }

    private void doChangeErrorResult(ErrorResult oldValue, ErrorResult newValue) {
        if (oldValue == newValue) return;
        if (errorHandler != null) {
            errorHandler.handle(oldValue, newValue);
        }
        errorResult = newValue;
    }

    public final void bindErrorHandler(ErrorHandler handler) {
        if (errorHandler == handler) return;
        errorHandler = handler;
        doChangeErrorResult(null, errorResult);
    }

    public final void setErrorResult(ErrorResult result) {
        doChangeErrorResult(errorResult, result == null || !result.isErrorState() ? null : result);
    }

    public final DataObject getParent() {
        return parent;
    }

    public final DataLocation getLocation() {
        return location;
    }

    public abstract DataField getField(String fieldName);

    public abstract Iterable<FieldEntry> getFields();

    public JsonNode getNode() {
        if (node != null) return node;
        node = Utils.getSortedMapper().createObjectNode();
        for (var entry : getFields()) {
            ((ObjectNode) node).set(entry.getName(), DataField.getNode(entry.getField()));
        }
        return node;
    }

    public final boolean hasErrors() {
        return errorResult != null && errorResult.isErrorState();
    }
}
