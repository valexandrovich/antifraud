package ua.com.solidity.notification.service;

public interface EmailService {

    void sendSimpleMessage(String to, String subject, String text, int retries);

    void sendSimpleMessageUsingTemplate(String to, String subject, String[] templateModel, int retries);

    void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment, int retries);

}
