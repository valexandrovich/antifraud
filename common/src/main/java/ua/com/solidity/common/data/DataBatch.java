package ua.com.solidity.common.data;

import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.Utils;
import ua.com.solidity.pipeline.Item;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DataBatch {
    public static final int DEFAULT_OBJECT_COUNT = 16384;
    public static final int DEFAULT_ERROR_COUNT = 1024;
    public static final int DEFAULT_CAPACITY = 16384;

    private static int LOADED_DEFAULT_CAPACITY = -1;

    public interface FlushHandler {
        void flush(DataBatch obj);
    }

    public interface ObjectHandler {
        ErrorResult handle(DataObject value);
    }

    public interface ModifyHandler {
        DataObject modify(DataObject value);
    }

    public interface ErrorHandler {
        boolean handle(ErrorReport value);
    }

    private final Item item;
    private final int capacity;
    private int count = 0;
    private int objectCount = 0;
    private int errorCount = 0;
    private final int objectCapacity;
    private final int errorCapacity;
    private int insertErrorCount;
    private int badObjectCount = 0;
    private final List<Object> instances = new ArrayList<>();
    private final FlushHandler handler;
    private DataExtensionFactory extensionFactory = null;

    public DataBatch(int capacity, int objectCapacity, int errorCapacity, Item item, FlushHandler handler) {
        int defCapacity = getDefaultCapacity();

        if (capacity < 1 && objectCapacity < 1 && errorCapacity < 1) {
            capacity = defCapacity + DEFAULT_ERROR_COUNT;
        }

        this.objectCapacity = objectCapacity < 1 ? defCapacity : objectCapacity;
        this.errorCapacity = errorCapacity < 1 ? DEFAULT_ERROR_COUNT : errorCapacity;
        this.capacity = capacity < 1 ? this.objectCapacity + this.errorCapacity : capacity;
        this.handler = handler;
        this.insertErrorCount = 0;
        this.item = item;
    }

    private static int getDefaultCapacity() {
        if (LOADED_DEFAULT_CAPACITY < 1) {
            int value = DEFAULT_CAPACITY;
            try {
                value = Integer.parseInt(Utils.getContextProperty("importer.batch.size",
                        String.valueOf(DEFAULT_CAPACITY)));
            } catch (Exception e) {
                // nothing
            }
            LOADED_DEFAULT_CAPACITY = value;
        }
        return LOADED_DEFAULT_CAPACITY;
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
            count = 0;
        }
    }

    public void put(DataObject object) {
        if (object == null) return;
        if (extensionFactory != null) {
            extensionFactory.handle(object);
        }
        instances.add(object);
        ++objectCount;
        if (objectCount == objectCapacity || instances.size() == capacity) {
            doFlush();
        }
    }

    public void put(ErrorReport report) {
        if (report == null) return;
        instances.add(report);
        ++errorCount;
        if (errorCount == errorCapacity || instances.size() == capacity) {
            doFlush();
        }
    }

    public final int getBadObjectCount() {
        return badObjectCount;
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

    public final int getCommittedCount() {
        return objectCount - badObjectCount;
    }

    public final int getErrorCount() {
        return errorCount;
    }

    public void clear() {
        objectCount = 0;
        errorCount = 0;
        count = 0;
        badObjectCount = 0;
        insertErrorCount = 0;
        instances.clear();
    }

    public final boolean isEmpty() {
        return instances.isEmpty();
    }

    public final void flush() {
        if (!isEmpty()) {
            doFlush();
        }
    }

    private void handleDataObject(ObjectHandler objectHandler, ErrorHandler errorHandler, DataObject obj) {
        ErrorResult res = objectHandler.handle(obj);
        if (res.isErrorState()) {
            ++badObjectCount;
            if (errorHandler != null) {
                for (var report : res.getReport()) {
                    errorHandler.handle(report);
                }
            }
            errorCount += res.getReport().size();
            insertErrorCount += res.getReport().size();
        }
    }

    public final void handle(ObjectHandler objectHandler, ErrorHandler errorHandler) {
        for (Object obj : instances) {
            List<ErrorReport> reports;
            if (obj instanceof DataObject) {
                handleDataObject(objectHandler, errorHandler, (DataObject) obj);
            } else if (obj instanceof ErrorReport && errorHandler != null) {
                errorHandler.handle((ErrorReport) obj);
            }
        }
    }

    public final void modify(ModifyHandler modifyHandler) {
        if (modifyHandler != null) {
            instances.replaceAll(obj->obj instanceof DataObject ? modifyHandler.modify((DataObject) obj) : obj);
        }
    }
}
