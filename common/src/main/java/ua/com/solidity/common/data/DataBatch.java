package ua.com.solidity.common.data;

import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.Utils;
import ua.com.solidity.pipeline.Item;

import java.util.*;

public class DataBatch {
    public static final int DEFAULT_CAPACITY = 16384; // for objects
    private static int loadedCapacity = -1;

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

    @Getter
    @Setter
    private Item item;

    @Getter
    private UUID portion = null;

    @Getter
    @Setter
    private String source;
    private final int capacity;
    private int objectCount = 0;
    private int errorCount = 0;
    private int badObjectCount = 0;
    private int objectErrorsCount = 0;
    private final List<Object> instances = new ArrayList<>();
    private final FlushHandler handler;
    private DataExtensionFactory extensionFactory = null;

    public DataBatch(int capacity, FlushHandler handler) {
        if (capacity < 1) {
            capacity = getDefaultCapacity();
        }
        this.capacity = capacity;
        this.handler = handler;
    }

    private static int getDefaultCapacity() {
        if (loadedCapacity < 1) {
            int value = DEFAULT_CAPACITY;
            try {
                value = Integer.parseInt(Utils.getContextProperty("importer.batch.size", String.valueOf(DEFAULT_CAPACITY)));
            } catch (Exception e) {
                // nothing
            }
            loadedCapacity = value;
        }
        return loadedCapacity;
    }

    private void doSetExtensionFactory(DataExtensionFactory factory) {
        for (var instance: instances) {
            if (instance instanceof DataObject) {
                DataObject obj = (DataObject) instance;
                DataExtension ext = factory.createExtension();
                ext.setPortion(getPortion());
                obj.setExtension(ext);
            }
        }
    }

    private void doSetExtensionFactoryToNull() {
        for (var instance: instances) {
            if (instance instanceof DataObject) {
                DataObject obj = (DataObject) instance;
                obj.setExtension(null);
            }
        }
    }

    public final void setExtensionFactory(DataExtensionFactory factory) {
        if (this.extensionFactory != factory) {
            if (factory != null) {
                doSetExtensionFactory(factory);
            } else {
                doSetExtensionFactoryToNull();
            }
        }
        this.extensionFactory = factory;
    }

    public final void setPortion(UUID portion) {
        if (this.portion == portion) return;
        for (var instance: instances) {
            if (instance instanceof DataObject) {
                DataObject obj = (DataObject) instance;
                DataExtension ext = obj.getExtension();
                if (ext != null) {
                    ext.setPortion(portion);
                }
            }
        }
        this.portion = portion;
    }

    public final DataExtensionFactory getExtensionFactory() {
        return this.extensionFactory;
    }

    private void doFlush() {
        try {
            if (handler != null) {
                handler.flush(this);
            }
        } finally {
            clear();
        }
    }

    private void handleError(ErrorResult oldValue, ErrorResult newValue) {
        if (oldValue == newValue) return;
        if (oldValue != null && oldValue.isErrorState()) {
            ++objectCount;
            --badObjectCount;
            objectErrorsCount -= oldValue.getErrorCount();
        }

        if (newValue != null && newValue.isErrorState()) {
            --objectCount;
            ++badObjectCount;
            objectErrorsCount += newValue.getErrorCount();
        }
    }

    public void put(DataObject object) {
        if (object == null) return;
        if (extensionFactory != null) {
            DataExtension ext = extensionFactory.handle(object);
            if (getPortion() != null) {
                ext.setPortion(getPortion());
            }
        }

        object.bindErrorHandler(this::handleError);
        instances.add(object);

        if (object.hasErrors()) {
            ++badObjectCount;
            ErrorResult res = object.getErrorResult();
            objectErrorsCount += res.getReport().size();
        } else {
            ++objectCount;
        }

        if (instances.size() == capacity) {
            doFlush();
        }
    }

    public void put(ErrorReport report) {
        if (report == null) return;
        instances.add(report);
        ++errorCount;
        if (instances.size() == capacity) {
            doFlush();
        }
    }

    public Object get(int index) {
        return index < 0 || index >= instances.size() ? null : instances.get(index);
    }

    public void remove(int index) {
        if (index >= 0 && index < instances.size() && instances.get(index) instanceof DataObject) {
            instances.remove(index);
        }
    }

    public void remove(Object obj) {
        instances.remove(obj);
    }

    public void pack() {
        for (int i = instances.size() - 1; i >= 0; --i) {
            Object obj = instances.get(i);
            if (obj instanceof DataObject) {
                DataObject dataObject = (DataObject) obj;
                if (dataObject.hasErrors()) {
                    remove(i);
                }
            } else {
                remove(i);
            }
        }
    }

    public final int getBadObjectCount() {
        return badObjectCount;
    }

    public final int getObjectErrorsCount() {
        return objectErrorsCount;
    }

    public final int getObjectCount() {
        return objectCount;
    }

    @SuppressWarnings("unused")
    public final int getCommittedCount() {
        return objectCount - badObjectCount;
    }

    public final int getErrorCount() {
        return errorCount;
    }

    public void clear() {
        objectCount = errorCount = badObjectCount = objectErrorsCount = 0;
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
        if (res != null && res.isErrorState()) {
            ++badObjectCount;
            if (errorHandler != null) {
                for (var report : res.getReport()) {
                    errorHandler.handle(report);
                    ++objectErrorsCount;
                }
            }
        }
    }

    public final void handle(ObjectHandler objectHandler, ErrorHandler errorHandler) {
        for (Object obj : instances) {
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

    public final long size() {
        return instances.size();
    }

    public final Iterable<DataObject> validObjects() {
        return () -> new Iterator<>() {
            private static final int UNINITIALIZED = -1;
            private static final int OUT_OF_RANGE = -2;
            int index = UNINITIALIZED;

            private void findNextObject() {
                for (int i = index + 1; i < size(); ++i) {
                    Object obj = instances.get(i);
                    if (obj instanceof DataObject) {
                        DataObject dataObject = (DataObject) obj;
                        ErrorResult res = dataObject.getErrorResult();
                        if (res == null || !res.isErrorState()) {
                            index = i;
                            return;
                        }
                    }
                }
                index = OUT_OF_RANGE;
            }

            @Override
            public boolean hasNext() {
                if (index == UNINITIALIZED) {
                    findNextObject();
                }
                return index >= 0;
            }

            @Override
            public DataObject next() {
                if (index >= 0) {
                    DataObject obj = (DataObject) instances.get(index);
                    findNextObject();
                    return obj;
                }
                return null;
            }
        };
    }

}
