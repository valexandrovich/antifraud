package ua.com.solidity.common;

public interface RabbitMQActionHandler {
    void handle(RabbitMQActionTask task);
}
