package ua.com.solidity.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DeferredTask {
    protected abstract DeferredAction compareWith(DeferredTask task);
    public void run() {
        execute();
    }
    protected abstract void execute();
}
