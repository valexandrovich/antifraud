package ua.com.solidity.common;

import com.rabbitmq.client.Delivery;
import lombok.CustomLog;

import java.nio.charset.StandardCharsets;

@CustomLog
public abstract class RabbitMQReceiver {
    Delivery message;
    RabbitMQListener listener;
    boolean acknowledgeSent = false;
    protected abstract Object handleMessage(String queue, String message);

    synchronized Object doHandleMessage(RabbitMQListener listener, Delivery message) {
        this.listener = listener;
        this.message = message;
        this.acknowledgeSent = false;
        Object res;
        String routingKey = message.getEnvelope().getRoutingKey();
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            res = handleMessage(routingKey, msg);
        } catch (Exception e) {
            log.error("RabbitMQListener message handling error (routingKey: {}, message:{})", routingKey, msg, e);
            res = true;
        }
        this.listener = null;
        this.message = null;
        return res;
    }

    protected final void send(String queue, String message) {
        Utils.sendRabbitMQMessage(queue, message);
    }

    @SuppressWarnings("unused")
    public final void acknowledge(boolean ack) {
        if (listener != null) {
            listener.doAcknowledge(message.getEnvelope().getDeliveryTag(), ack);
            acknowledgeSent = true;
        }
    }
}
