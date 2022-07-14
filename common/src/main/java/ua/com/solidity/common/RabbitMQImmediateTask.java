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
    protected void rmqExecute() {
        RabbitMQListener listener;
        synchronized (listener = getListener()) {
            listener.receiver.prepareInternalMessage(getListener(), getMessage());
            listener.receiver.handleMessage(getExchange(), getMessageBody());
        }
    }
}
