package ua.com.solidity.otp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class EntityExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<ExceptionResponse> entityAlreadyExistHandler(EntityNotFoundException ex) {
        return new ResponseEntity<ExceptionResponse>(
                ExceptionResponse.builder().code("404").text(ex.getMessage()).build(),
                HttpStatus.NOT_FOUND);
    }
}
