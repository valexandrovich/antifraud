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
    protected void rmqExecute() {
        if (action != null && handler != null) {
            handler.handle(this);
        }
    }
}
