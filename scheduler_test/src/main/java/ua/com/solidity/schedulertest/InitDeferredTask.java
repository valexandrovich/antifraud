package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.DeferredAction;
import ua.com.solidity.common.DeferredTask;
import ua.com.solidity.common.RabbitMQTask;
import ua.com.solidity.common.Utils;

@Slf4j
public class InitDeferredTask extends RabbitMQTask {
    private final Config config;
    private final String msg;

    public InitDeferredTask(Config config, String msg) {
        super(false);
        this.config = config;
        this.msg = msg;
    }

    @Override
    public DeferredAction compareWith(DeferredTask task) {
        if (task instanceof InitDeferredTask) return DeferredAction.IGNORE;
        return DeferredAction.APPEND;
    }

    @Override
    protected void execute() {
        Utils.sendRabbitMQMessage(config.getScheduler(), msg);
        acknowledge(true);
        log.info("Init request handled with message - {}", msg);
    }
}
