package ua.com.solidity.common;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CustomLog
public abstract class DeferredTask {
    protected DeferredAction compareWith(DeferredTask task) {
        return DeferredAction.APPEND;
    }

    protected abstract void execute();

    protected abstract String description();
}
