package ua.com.solidity.web.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseBody<T> {

    private final T principal;

    private final int status;

    private final String message;

    public ResponseBody(T principal, HttpStatus status, String message) {
        this.principal = principal;
        this.status = status.value();
        this.message = message;
    }

}
