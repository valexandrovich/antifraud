package ua.com.solidity.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendEmailRequest {

    private String to;

    private String subject;

    private String body;

    private int retries;

    private String filePath;
}
