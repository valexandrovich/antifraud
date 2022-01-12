package ua.com.solidity.otp.web.response;

import org.springframework.http.HttpStatus;


public class Response {

    private final int status;

    private final String message;

    public Response(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

}
