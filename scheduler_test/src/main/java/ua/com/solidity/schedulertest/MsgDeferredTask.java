package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.DeferredAction;
import ua.com.solidity.common.DeferredTask;

@Slf4j
public class MsgDeferredTask implements DeferredTask {
    private final String msg;
    private long tag;

    public MsgDeferredTask(String msg) {
        this.msg = msg;
    }

    @Override
    public DeferredAction compareWith(DeferredTask task) {
        if (task instanceof MsgDeferredTask) {
            MsgDeferredTask msgTask = (MsgDeferredTask) task;
            if (msg.equals(msgTask.msg)) {
                return DeferredAction.IGNORE;
            }
        }
        return DeferredAction.APPEND;
    }

    @Override
    public boolean execute() {
        log.info("Receiver test msg: {}", msg);
        return true;
    }

    @Override
    public long getTag() {
        return tag;
    }

    @Override
    public void setTag(long tag) {
        this.tag = tag;
    }
}
