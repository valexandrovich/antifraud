package ua.com.solidity.scheduler;

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
    @Value("${scheduler.rabbitmq.queue}")
    private String queueName;
    @Value("${scheduler.rabbitmq.routingKey}")
    private String routingKey;
    @Value("${scheduler.rabbitmq.init.routingKey}")
    private String initRoutingKey;
}
