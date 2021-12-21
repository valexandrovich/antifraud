package ua.com.solidity.otp.web.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionResponse {
    private String code;
    private String text;

}
