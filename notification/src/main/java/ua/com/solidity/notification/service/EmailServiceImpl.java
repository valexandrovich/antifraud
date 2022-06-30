package ua.com.solidity.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
@PropertySource({"classpath:notification.properties", "classpath:application.properties"})
public class EmailServiceImpl implements EmailService {

    @Value("${noreply.address}")
    private String NOREPLY_ADDRESS;

    private final JavaMailSender emailSender;

    private final SimpleMailMessage template;

    @Override
    public void sendSimpleMessage(String to, String subject, String text, int retries) {
        int retriesCount = 0;
        do {
            retriesCount++;
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(NOREPLY_ADDRESS);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);

                emailSender.send(message);
                retries = 0;
            } catch (MailException exception) {
                retries--;
                if (retries == 0) {
                    log.error("Failed to send notification message after " + retriesCount + " attempts: {}", exception.getMessage());
                }
            }
        } while (retries > 0);
    }

    @Override
    public void sendSimpleMessageUsingTemplate(String to,
                                               String subject,
                                               String[] templateModel,
                                               int retries) {
        String text = String.format(Objects.requireNonNull(template.getText()), (Object) templateModel);
        sendSimpleMessage(to, subject, text, retries);
    }

    @Override
    public void sendMessageWithAttachment(String to,
                                          String subject,
                                          String text,
                                          String pathToAttachment,
                                          int retries) {
        int retriesCount = 0;
        do {
            retriesCount++;
            try {
                MimeMessage message = emailSender.createMimeMessage();
                // pass 'true' to the constructor to create a multipart message
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom(NOREPLY_ADDRESS);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text);

                FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
                helper.addAttachment("Attachment", file);

                emailSender.send(message);
                retries = 0;
            } catch (MessagingException exception) {
                retries--;
                if (retries == 0) {
                    log.error("Failed to send notification message after " + retriesCount + " attempts: {}", exception.getMessage());
                }
            }
        } while (retries > 0);
    }
}
