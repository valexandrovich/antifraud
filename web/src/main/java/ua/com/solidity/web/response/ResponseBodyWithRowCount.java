package ua.com.solidity.web.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseBodyWithRowCount<T> {

    private final T principal;

    private final int rowCount;

    private final int status;

    private final String message;

    public ResponseBodyWithRowCount(T principal, int rowCount, HttpStatus status, String message) {
        this.principal = principal;
        this.rowCount = rowCount;
        this.status = status.value();
        this.message = message;
    }

}
