package ua.com.solidity.otp.web.response;

import org.springframework.http.HttpStatus;


public class ResponseBody<T> {

    private final T principal;

    private final int status;

    private final String message;

    public ResponseBody(T principal, HttpStatus status, String message) {
        this.principal = principal;
        this.status = status.value();
        this.message = message;
    }

    public T getUuid() {
        return this.principal;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

}
