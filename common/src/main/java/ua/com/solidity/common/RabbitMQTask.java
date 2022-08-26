package ua.com.solidity.common;

import com.rabbitmq.client.Delivery;
import lombok.CustomLog;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

@CustomLog
@Getter
public abstract class RabbitMQTask extends DeferrableTask {
    protected RabbitMQListener listener;
    private Delivery message;
    private String messageBody;
    private boolean acknowledgeSent = false;

    final synchronized void setContext(RabbitMQListener listener, Delivery message) {
        this.listener = listener;
        this.message = message;
        this.messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
    }

    public final String getExchange() {
        return message.getEnvelope().getExchange();
    }

    @Override
    protected final void execute() { // acknowledge in RabbitMQListener
        if (listener != null) {
            try {
                acknowledge(rmqExecute());
            } catch (Exception e) {
                log.error("RabbitMQTask execution error for {}.", description(), e);
                acknowledge(false);
            } finally {
                listener = null;
                message = null;
            }
        }
    }

    protected boolean rmqExecute() {
        return false;
    }

    protected void acknowledge(boolean ack) {
        if (!acknowledgeSent) {
            if (listener != null) {
                listener.doAcknowledge(message.getEnvelope().getDeliveryTag(), ack);
            }
            acknowledgeSent = true;
        }
    }

    protected final void send(String queue, String message) {
        Utils.sendRabbitMQMessage(queue, message);
    }

    @SuppressWarnings("unused")
    protected final void enqueueBack() {
        Utils.sendRabbitMQMessage(getExchange(), getMessageBody());
    }
}
