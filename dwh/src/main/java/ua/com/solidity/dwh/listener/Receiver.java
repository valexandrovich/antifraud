package ua.com.solidity.dwh.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.dwh.model.UpdateDWHRequest;
import ua.com.solidity.dwh.service.DWHService;

@Slf4j
@Component
@RequiredArgsConstructor
public class Receiver extends RabbitMQReceiver {

    private final DWHService dwhService;

    @Override
    public Object handleMessage(String queue, String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        UpdateDWHRequest updateDWHRequest;
        try {
            updateDWHRequest = objectMapper.readValue(message, UpdateDWHRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Couldn't read object from queue: {}", e.getMessage());
            return true;
        }

        log.info("Received from {}: {}", queue, message);
        dwhService.update(updateDWHRequest.getLastModified());

        return true;
    }
}
