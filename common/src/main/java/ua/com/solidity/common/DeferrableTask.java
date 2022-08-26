package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CustomLog
public abstract class DeferrableTask {
    /**
     * @param task This may be use for further computation in overriding classes
     */
    protected DeferredAction compareWith(DeferrableTask task) {
        return DeferredAction.APPEND;
    }

    @JsonIgnore
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected abstract boolean isDeferred();

    protected abstract void execute();

    protected abstract String description();
}
