package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@Component
public class Receiver {
    private final RabbitTemplate rabbitTemplate;
    private final String schedulerInitMessage;
    private final Config config;

    @Autowired
    public Receiver(RabbitTemplate rabbitTemplate, String schedulerInitMessage, Config config) {
        this.rabbitTemplate = rabbitTemplate;
        this.schedulerInitMessage = schedulerInitMessage;
        this.config = config;
    }

    public void receiveInitMessage(String message) {
         rabbitTemplate.convertAndSend(config.getTopicExchangeName(), config.getActionRoutingKey(), schedulerInitMessage);
         log.info("Init request handled with message - {}", message);
    }

    public void receiveTestMessage(String message) {
        log.info("Received test message: {}", message);
    }
}
