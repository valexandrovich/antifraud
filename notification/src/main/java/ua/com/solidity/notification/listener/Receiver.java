package ua.com.solidity.notification.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.notification.model.SendEmailRequest;
import ua.com.solidity.notification.service.EmailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class Receiver extends RabbitMQReceiver {

    private final EmailService emailService;

    @Override
    public Object handleMessage(String queue, String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        SendEmailRequest sendEmailRequest = null;
        try {
            sendEmailRequest = objectMapper.readValue(message, SendEmailRequest.class);
        } catch (JsonProcessingException e) {
            return false;
        }

        emailService.sendSimpleMessage(sendEmailRequest.getTo(), sendEmailRequest.getSubject(), sendEmailRequest.getBody());
        log.info("Receive from " + queue + ": {}", message);

        return true;
    }
}
