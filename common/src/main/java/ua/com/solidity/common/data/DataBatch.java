package ua.com.solidity.common.data;

import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.pipeline.Item;

import java.util.Arrays;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class DataBatch {
    public static final int DEFAULT_OBJECT_COUNT = 16384;
    public static final int DEFAULT_ERROR_COUNT = 1024;

    public interface FlushHandler {
        void flush(DataBatch obj);
    }

    public interface ObjectHandler {
        boolean handle(DataObject value);
    }

    public interface ErrorHandler {
        boolean handle(ErrorReport value);
    }

    private final Item item;
    private final int capacity;
    private int count;
    private int objectCount;
    private int errorCount;
    private int insertErrorCount;
    private final DataObject[] objects;
    private final ErrorReport[] errors;
    private final FlushHandler handler;
    private DataExtensionFactory extensionFactory = null;

    public DataBatch(int capacity, int objectCapacity, int errorCapacity, Item item, FlushHandler handler) {
        if (capacity < 1) {
            if (objectCapacity < 1) {
                objectCapacity = DEFAULT_OBJECT_COUNT;
            }
            if (errorCapacity < 1) {
                errorCapacity = DEFAULT_ERROR_COUNT;
            }
        }

        objects = new DataObject[objectCapacity < 1 ? capacity : objectCapacity];
        errors = new ErrorReport[errorCapacity < 1 ? capacity : errorCapacity];
        this.handler = handler;
        objectCount = errorCount = insertErrorCount = 0;
        this.capacity = capacity <= 0 ? Math.max(objectCapacity, errorCapacity) : capacity;
        this.item = item;
    }

    public final void setExtensionFactory(DataExtensionFactory factory) {
        if (this.extensionFactory != null) return;
        this.extensionFactory = factory;
    }

    public final DataExtensionFactory getExtensionFactory() {
        return this.extensionFactory;
    }

    public final Item getItem() {
        return item;
    }

    private void doFlush() {
        try {
            if (handler != null) {
                handler.flush(this);
            }
        } finally {
            count = objectCount = errorCount = 0;
        }
    }

    public void put(DataObject object) {
        if (object == null) return;
        if (extensionFactory != null) {
            extensionFactory.handle(object);
        }
        objects[objectCount++] = object;
        ++count;
        if (objectCount == objects.length || count == capacity) {
            doFlush();
        }
    }

    public void put(ErrorReport report) {
        errors[errorCount++] = report;
        ++count;
        if (errorCount == errors.length || count == capacity) {
            doFlush();
        }
    }

    public final int getInsertErrorCount() {
        return insertErrorCount;
    }

    public final void addInsertErrorCount(int count) {
        insertErrorCount += count;
    }

    public final int getObjectCount() {
        return objectCount;
    }

    public final int getErrorCount() {
        return errorCount;
    }

    public void clear() {
        objectCount = 0;
        errorCount = 0;
    }

    public final DataObject get(int index) {
        return index >= 0 && index < objectCount ? objects[index] : null;
    }

    public final boolean replace(int index, DataObject newObject) {
        if (index >= 0 && index < objectCount) {
            objects[index] = newObject;
            return true;
        }
        return false;
    }

    public final ErrorReport getError(int index) {
        return index >= 0 && index < errorCount ? errors[index] : null;
    }

    public final boolean isEmpty() {
        return objectCount == 0 && errorCount == 0;
    }

    public final void flush() {
        if (!isEmpty()) {
            doFlush();
        }
    }

    public final int handleObjects(ObjectHandler handler) {
        int handled = 0;
        if (handler != null) {
            for (int i = 0; i < objectCount; ++i) {
                if (handler.handle(objects[i])) {
                    ++handled;
                }
            }
        }
        return handled;
    }

    public final int handleErrors(ErrorHandler handler) {
        int handled = 0;
        if (handler != null) {
            for (int i = 0; i < errorCount; ++i) {
                if (handler.handle(errors[i])) {
                    ++handled;
                }
            }
        }
        return handled;
    }

    public final Stream<DataObject> objectStream() {
        return Arrays.stream(objects, 0, objectCount);
    }

    public final Stream<ErrorReport> errorStream() {
        return Arrays.stream(errors, 0, errorCount);
    }
}
