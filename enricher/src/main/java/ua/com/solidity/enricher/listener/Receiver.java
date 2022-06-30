package ua.com.solidity.enricher.listener;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.common.Utils;
import ua.com.solidity.enricher.model.EnricherPortionRequest;
import ua.com.solidity.enricher.service.EnricherService;

@CustomLog
@Component
@RequiredArgsConstructor
public class Receiver extends RabbitMQReceiver {

    private final EnricherService enricherService;

    @Override
    public Object handleMessage(String queue, String message) {
        acknowledge(true); // no deferred tasks, no unacknowledged situations
        EnricherPortionRequest request = Utils.jsonToValue(message, EnricherPortionRequest.class);
        if (request != null) {
            log.info("$debug$Received message from {}: {}", queue, message);
            enricherService.enrich(request);
        }
        return true;
    }
}
