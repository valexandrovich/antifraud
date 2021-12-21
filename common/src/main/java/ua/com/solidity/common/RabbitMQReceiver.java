package ua.com.solidity.common;

public abstract class RabbitMQReceiver {
    protected abstract Object handleMessage(String queue, String message);
    protected final void send(String queue, String message) {
        Utils.sendRabbitMQMessage(queue, message);
    }
}
