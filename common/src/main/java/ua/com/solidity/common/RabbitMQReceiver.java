package ua.com.solidity.common;

import com.rabbitmq.client.Delivery;
import lombok.CustomLog;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

@CustomLog
public abstract class RabbitMQReceiver {
    String extractedMessage;
    @Getter
    Delivery message;
    @Getter
    RabbitMQListener listener;

    @SuppressWarnings("unused")
    protected void enqueueBack() {
        Utils.sendRabbitMQMessage(message.getEnvelope().getExchange(), extractedMessage);
    }

    protected void handleMessage(String queue, String message) {
        // nothing
    }

    protected String getExchange() {
        return message == null ? null : message.getEnvelope().getExchange();
    }

    protected void prepareInternalMessage(RabbitMQListener listener, Delivery message) {
        this.listener = listener;
        this.message = message;
        this.extractedMessage = new String(message.getBody(), StandardCharsets.UTF_8);
    }

    protected RabbitMQTask createActionTask(ActionObject action, RabbitMQActionHandler handler) {
        return action != null && handler != null ? new RabbitMQActionTask(listener, message, action, handler) : null;
    }

    @SuppressWarnings("unused")
    protected RabbitMQTask createTask(String queue, String message) {
        return null;
    }

    synchronized RabbitMQTask doHandleMessage(RabbitMQListener listener, Delivery message) {
        prepareInternalMessage(listener, message);
        RabbitMQTask res;
        try {
            res = createTask(getExchange(), extractedMessage);
            if (res == null) {
                res = new RabbitMQImmediateTask(listener, message);
            }
        } catch (Exception e) {
            log.error("RabbitMQListener message handling error (routingKey: {}, message:{})", getExchange(), extractedMessage, e);
            res = null;
        }
        return res;
    }

    protected final void send(String queue, Object obj) {
        Utils.sendRabbitMQMessage(queue, obj);
    }
}
