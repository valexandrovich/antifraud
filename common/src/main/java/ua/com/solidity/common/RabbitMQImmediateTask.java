package ua.com.solidity.common;

import com.rabbitmq.client.Delivery;

public class RabbitMQImmediateTask extends RabbitMQTask {

    protected RabbitMQImmediateTask(RabbitMQListener listener, Delivery message) {
        super();
        setContext(listener, message);
    }

    @Override
    public boolean isDeferred() {
        return false;
    }

    @Override
    protected String description() {
        return Utils.messageFormat("<Receiver.handleMessage for {}:{}>",
                getMessage().getEnvelope().getExchange(), getMessage().getEnvelope().getRoutingKey());
    }

    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    protected boolean rmqExecute() {
        if (listener != null) {
            synchronized (listener) {
                return listener.receiver.prepareAndHandleInternalMessage(this);
            }
        }
        return false;
    }
}
