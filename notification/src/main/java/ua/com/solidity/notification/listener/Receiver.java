package ua.com.solidity.notification.listener;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.common.Utils;
import ua.com.solidity.notification.model.SendEmailRequest;
import ua.com.solidity.notification.service.EmailService;

@CustomLog
@Component
@RequiredArgsConstructor
public class Receiver extends RabbitMQReceiver {

    private final EmailService emailService;

    @Override
    public void handleMessage(String queue, String message) {
        log.info("Received from " + queue + ": {}", message);
        SendEmailRequest sendEmailRequest = Utils.jsonToValue(message, SendEmailRequest.class);
        if (sendEmailRequest == null) return;

        int retries = Math.min(sendEmailRequest.getRetries(), 3);
        try {
            if (StringUtils.isBlank(sendEmailRequest.getFilePath())) {
                emailService.sendSimpleMessage(
                        sendEmailRequest.getTo(),
                        sendEmailRequest.getSubject(),
                        sendEmailRequest.getBody(),
                        retries);
            } else {
                emailService.sendMessageWithAttachment(
                        sendEmailRequest.getTo(),
                        sendEmailRequest.getSubject(),
                        sendEmailRequest.getBody(),
                        sendEmailRequest.getFilePath(),
                        retries);
            }
        } catch (Exception e) {
            log.error("Error on handling sendEmailRequest.", e);
        }
    }
}