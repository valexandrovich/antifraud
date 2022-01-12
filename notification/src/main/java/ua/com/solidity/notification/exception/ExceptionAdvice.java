package ua.com.solidity.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(JsonNotConvertedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ExceptionResponse JsonNotConvertedHandler(JsonNotConvertedException ex) {
        return ExceptionResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
    }

}
