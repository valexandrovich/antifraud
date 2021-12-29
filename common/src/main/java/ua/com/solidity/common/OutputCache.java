package ua.com.solidity.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Getter
@Setter
public class OutputCache {
    private OutputStats.Group group;
    private Object[] objectCache;
    private ErrorReport[] errorCache;
    private int objectCacheSize = 0;
    private int errorCacheSize = 0;
    private int handledObjectsCount = 0;
    private boolean objectCacheBOF = true;
    private boolean errorCacheBOF = true;
    private Object[] handledObjects;

    public OutputCache(OutputStats.Group group, int objectCacheSize, int errorCacheSize) {
        this.group = group;
        this.objectCache = new Object[Math.max(objectCacheSize, 1)];
        this.handledObjects = new Object[this.objectCache.length];
        this.errorCache = new ErrorReport[Math.max(errorCacheSize, 1)];
    }

    public final OutputStats.Group getGroup() {
        return group;
    }

    public final boolean putObject(Object obj) { // returns flush needed
        objectCache[objectCacheSize++] = obj;
        return objectCacheSize == objectCache.length;
    }

    public final int getObjectCacheSize() {
        return objectCacheSize;
    }

    public final Object getObject(int index) {
        return index >= 0 && index < objectCacheSize ? objectCache[index] : null;
    }

    public final <T> T getObject(int index, Class<T> type) {
        Object obj = getObject(index);
        return type.cast(obj);
    }

    @SuppressWarnings("unused")
    public final boolean isFirstObjectCache() {
        return objectCacheBOF;
    }

    public final void objectCacheHandled(int committed) {
        int inserted = getHandledObjectCount();
        int ignored = committed - inserted;
        group.incInsertCount(getHandledObjectCount());
        group.incInsertIgnoreCount(ignored);
        group.incInsertErrorCount(objectCacheSize - ignored - inserted);
        Arrays.fill(objectCache, null);
        Arrays.fill(handledObjects, null);
        handledObjectsCount = 0;
        objectCacheSize = 0;
        objectCacheBOF = false;
    }

    public final void addHandledObject(Object obj) {
        if (obj != null) {
            handledObjects[handledObjectsCount++] = obj;
        }
    }

    public final void addHandledObjectByIndex(int index) {
        if (index >= 0 && index < objectCacheSize) {
            handledObjects[handledObjectsCount++] = objectCache[index];
        }
    }

    public final int getHandledObjectCount() {
        return handledObjectsCount;
    }

    public final Object getHandledObject(int index) {
        return index < 0 || index >= handledObjectsCount ? null : handledObjects[index];
    }

    @SuppressWarnings("unused")
    public final <T> T getHandledObject(int index, Class<T> type) {
        Object obj = getHandledObject(index);
        return type.cast(obj);
    }

    public final boolean putError(ErrorReport report) { // returns flush needed
        errorCache[errorCacheSize++] = report;
        return errorCacheSize == errorCache.length;
    }

    public final int getErrorCacheSize() {
        return errorCacheSize;
    }

    public final ErrorReport getErrorReport(int index) {
        return index >= 0 && index < errorCacheSize ? errorCache[index] : null;
    }

    @SuppressWarnings("unused")
    public final boolean isFirstErrorCache() {
        return errorCacheBOF;
    }

    public final void errorCacheHandled(int committed) {
        if (committed < errorCacheSize) {
            group.incInsertErrorInfoCount(errorCacheSize - committed);
        }
        group.incParseErrorCount(committed);
        errorCacheSize = 0;
        errorCacheBOF = false;
    }
}
