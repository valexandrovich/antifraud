package ua.com.solidity.notification.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ua.com.solidity.notification.model.SendEmailRequest;
import ua.com.solidity.notification.service.EmailService;
import ua.com.solidity.notification.utils.QueueCreds;

@Slf4j
@EnableRabbit
@RequiredArgsConstructor
@Component
public class RabbitMqListener {

    private final EmailService emailService;

    @RabbitListener(queues = QueueCreds.QUEUE_NAME)
    public void processMyQueue(String request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SendEmailRequest sendEmailRequest = objectMapper.readValue(request, SendEmailRequest.class);

        emailService.sendSimpleMessage(sendEmailRequest.getTo(), sendEmailRequest.getSubject(), sendEmailRequest.getBody());
        log.info("Receive from " + QueueCreds.QUEUE_NAME + ": {}", request);
    }

}
