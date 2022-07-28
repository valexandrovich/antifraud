package ua.com.solidity.common;

public interface RabbitMQActionHandler {
    boolean handle(RabbitMQActionTask task);
}
