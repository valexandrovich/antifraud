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
    RabbitMQTask currentTask;

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

    protected boolean prepareAndHandleInternalMessage(RabbitMQImmediateTask task) {
        this.listener = task.getListener();
        this.message = task.getMessage();
        this.extractedMessage = task.getMessageBody();
        this.currentTask = task;

        try {
            handleMessage(task.getExchange(), task.getMessageBody());
            return true;
        } catch (Exception e) {
            log.error("RabbitMQReceiver handleMessage error.", e);
            return false;
        }
    }

    protected RabbitMQTask createActionTask(ActionObject action, RabbitMQActionHandler handler) {
        return action != null && handler != null ? new RabbitMQActionTask(listener, message, action, handler) : null;
    }

    protected RabbitMQTask createActionTask(ActionObject action) {
        return createActionTask(action, this::defaultHandleAction);
    }

    protected boolean handleAction(ActionObject action) {
        log.info("-- Action requested ({}).", action.getNode());
        boolean res = action.execute();
        if (res) {
            log.info("  *Action completed.*");
        } else {
            log.info("  -Action {} is invalid or some errors occurred due to execution.-", action.getNode());
        }
        return res;
    }

    private boolean defaultHandleAction(RabbitMQActionTask task) {
        return handleAction(task.getAction());
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

    @SuppressWarnings("unused")
    protected final void acknowledge(boolean value) {
        if (currentTask == null) return;
        currentTask.acknowledge(value);
    }
}
