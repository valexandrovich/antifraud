package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.DeferredAction;
import ua.com.solidity.common.DeferredTask;
import ua.com.solidity.common.Utils;

@Slf4j
public class InitDeferredTask implements DeferredTask {
    private final Config config;
    private final String msg;
    private long tag;

    public InitDeferredTask(Config config, String msg) {
        this.config = config;
        this.msg = msg;
    }

    @Override
    public DeferredAction compareWith(DeferredTask task) {
        if (task instanceof InitDeferredTask) return DeferredAction.IGNORE;
        return DeferredAction.APPEND;
    }

    @Override
    public boolean execute() {
        Utils.sendRabbitMQMessage(config.getScheduler(), msg);
        log.info("Init request handled with message - {}", msg);
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
