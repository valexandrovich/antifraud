package ua.com.solidity.web.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class ExceptionResponse {

    private int statusCode;
    private HttpStatus status;
    private String message;

}
