package ua.com.solidity.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.notification.model.SendEmailRequest;

import static ua.com.solidity.notification.utils.QueueCreds.QUEUE_NAME;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RabbitController {

    private final AmqpTemplate template;

    @PostMapping("/emit")
    public ResponseEntity<String> emit(@RequestBody SendEmailRequest request) {
        log.info("Emit to myQueue");
        template.convertAndSend(QUEUE_NAME, request.toString());
        return ResponseEntity.ok("Success emit to queue");
    }
}
