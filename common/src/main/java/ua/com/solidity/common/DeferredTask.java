package ua.com.solidity.common;
public interface DeferredTask {
    DeferredAction compareWith(DeferredTask task);
    boolean execute();
    long getTag();
    void setTag(long tag);
}
