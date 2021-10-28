package ua.com.solidity.schedulertest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class Config {
    @Value("${scheduler.rabbitmq.topic}")
    private String topicExchangeName;
    @Value("${scheduler.rabbitmq.queue.init}")
    private String queueNameForInit;
    @Value("${scheduler.rabbitmq.queue.test}")
    private String queueNameForTest;
    @Value("${scheduler.rabbitmq.init.routingKey}")
    private String initRoutingKey;
    @Value("${scheduler.rabbitmq.routingKey}")
    private String actionRoutingKey;
    @Value("${scheduler.rabbitmq.test.routingKey}")
    private String testRoutingKey;
    @Value("${scheduler.schedulerInitFile}")
    private String schedulerInitFile;
}
