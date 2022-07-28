package ua.com.solidity.enricher.listener;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
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

    @RabbitListener(queues = OtpExchange.ENRICHER)
    public void processMyQueue(Message message) {
        EnricherPortionRequest request = Utils.jsonToValue(new String(message.getBody(), StandardCharsets.UTF_8), EnricherPortionRequest.class);

        if (request != null) {
            log.info("$debug$Received message from {}: {}", OtpExchange.ENRICHER, message);
            try {
                enricherProxy.direct(request);
            } catch (Exception e) {
                log.error("RabbitMQListener message handling error (routingKey: {}, message:{})", OtpExchange.ENRICHER, message, e);
            }
        }
    }
}
