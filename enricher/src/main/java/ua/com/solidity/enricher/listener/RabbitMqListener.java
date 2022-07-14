package ua.com.solidity.enricher.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.OtpExchange;
import ua.com.solidity.common.Utils;
import ua.com.solidity.enricher.model.EnricherPortionRequest;
import ua.com.solidity.enricher.service.EnricherProxy;

@Slf4j
@EnableRabbit
@Component
@RequiredArgsConstructor
public class RabbitMqListener {

    private final EnricherProxy enricherProxy;
    private final String enricherQueue = OtpExchange.ENRICHER;

    @RabbitListener(queues = enricherQueue)
    public void processMyQueue(String message) {
        EnricherPortionRequest request = Utils.jsonToValue(message, EnricherPortionRequest.class);
        if (request != null) {
            log.info("$debug$Received message from {}: {}", enricherQueue, message);
            try {
                enricherProxy.direct(request);
            } catch (Exception e) {
                log.error("RabbitMQListener message handling error (routingKey: {}, message:{})", enricherQueue, message, e);
            }
        }
    }
}
