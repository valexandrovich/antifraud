package ua.com.solidity.enricher.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.enricher.model.EnricherRequest;
import ua.com.solidity.enricher.service.EnricherService;

@Slf4j
@Component
@RequiredArgsConstructor
public class Receiver extends RabbitMQReceiver {

    private final EnricherService enricherService;

    @Override
    public Object handleMessage(String queue, String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        EnricherRequest enricherRequest;
        try {
            enricherRequest = objectMapper.readValue(message, EnricherRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Can't understand object from queue!", e);
            log.debug("The message was: {}", message);
            return true;
        }

        log.info("Received from {}: {}", queue, message);
        enricherService.enrich(enricherRequest);

        return true;
    }
}
