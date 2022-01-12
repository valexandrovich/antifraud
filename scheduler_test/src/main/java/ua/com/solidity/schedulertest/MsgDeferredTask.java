package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.DeferredAction;
import ua.com.solidity.common.DeferredTask;
import ua.com.solidity.common.RabbitMQTask;

@Slf4j
public class MsgDeferredTask extends RabbitMQTask {
    private final String msg;

    public MsgDeferredTask(String msg) {
        super(true);
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
    protected void execute() {
        log.info("Receiver test msg: {}", msg);
    }
}
