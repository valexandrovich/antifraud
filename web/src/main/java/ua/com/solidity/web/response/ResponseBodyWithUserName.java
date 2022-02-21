package ua.com.solidity.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseBodyWithUserName<T> {

    private final T principal;

    private final String userName;

    private final int status;

    private final String message;

}
