package ua.com.solidity.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class RabbitMQTask extends DeferredTask {
    private RabbitMQListener listener;
    private long deliveryTag;
    private final boolean autoAck;
    private boolean acknowledgeSent = false;

    protected RabbitMQTask(boolean autoAck) {
        this.autoAck = autoAck;
    }

    final synchronized RabbitMQTask setContext(RabbitMQListener listener, long deliveryTag) {
        this.listener = listener;
        this.deliveryTag = deliveryTag;
        return this;
    }

    private synchronized void internalExecute() {
        try {
            execute();
        } catch (Exception e) {
            log.error("RabbitMQTask execution error.", e);
        } finally {
            if (!acknowledgeSent) acknowledge(true);
            listener = null;
            deliveryTag = 0;
        }
    }

    @Override
    public void run() {
        if (listener != null) {
            if (autoAck) {
                listener.doAcknowledge(deliveryTag, true);
                acknowledgeSent = true;
            }
            internalExecute();
        }
    }

    protected final void acknowledge(boolean ack) {
        if (listener != null && !acknowledgeSent) {
            listener.doAcknowledge(deliveryTag, ack);
            acknowledgeSent = true;
        }
    }

    protected final void send(String queue, String message) {
        Utils.sendRabbitMQMessage(queue, message);
    }
}
