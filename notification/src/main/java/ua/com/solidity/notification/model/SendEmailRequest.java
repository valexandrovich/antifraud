package ua.com.solidity.notification.model;

import lombok.Data;


@Data
public class SendEmailRequest {

    private String to;

    private String subject;

    private String body;

    private int retries;

    private String filePath;
}
