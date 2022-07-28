package ua.com.solidity.common;

import com.rabbitmq.client.Delivery;
import lombok.CustomLog;
import lombok.Getter;

@CustomLog
public class RabbitMQActionTask extends RabbitMQImmediateTask {
    @Getter
    private final ActionObject action;
    private final RabbitMQActionHandler handler;

    protected RabbitMQActionTask(RabbitMQListener listener, Delivery message, ActionObject action, RabbitMQActionHandler handler) {
        super(listener, message);
        this.handler = handler;
        this.action = action;
    }

    @Override
    protected boolean rmqExecute() {
        if (action != null && handler != null) {
            try {
                handler.handle(this);
                return true;
            } catch (Exception e) {
                log.error("Action Execution failed.", e);
                return false;
            }
        }
        return false;
    }
}
