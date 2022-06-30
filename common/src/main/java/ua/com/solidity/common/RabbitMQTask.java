package ua.com.solidity.common;

import com.rabbitmq.client.Delivery;
import lombok.CustomLog;
import lombok.Getter;

@CustomLog
@Getter
public abstract class RabbitMQTask extends DeferredTask {
    private RabbitMQListener listener;
    private Delivery message;
    private final boolean autoAck;
    private boolean acknowledgeSent = false;

    protected RabbitMQTask(boolean autoAck) {
        this.autoAck = autoAck;
    }

    final synchronized void setContext(RabbitMQListener listener, Delivery message, boolean acknowledgeSent) {
        this.listener = listener;
        this.message = message;
        this.acknowledgeSent = acknowledgeSent;
    }
    
    @Override
    protected final void execute() {
        if (autoAck) {
            acknowledge(true);
        }

        if (listener != null) {
            try {
                rmqExecute();
            } catch (Exception e) {
                log.error("RabbitMQTask execution error for {}.", description(), e);
            } finally {
                if (!acknowledgeSent) acknowledge(true);
                listener = null;
                message = null;
            }
        }
    }
    @SuppressWarnings("SameParameterValue")
    protected void setAcknowledgeSent(boolean value) {
        acknowledgeSent = value;
    }

    protected void rmqExecute() {
        // nothing yet
    }

    protected final void acknowledge(boolean ack) {
        if (listener != null && !acknowledgeSent) {
            listener.doAcknowledge(message.getEnvelope().getDeliveryTag(), ack);
            acknowledgeSent = true;
        }
    }

    protected final void send(String queue, String message) {
        Utils.sendRabbitMQMessage(queue, message);
    }
}
