package ua.com.solidity.web.security.exception;

import org.springframework.security.core.AuthenticationException;
import ua.com.solidity.web.security.token.JwtToken;


public class JwtTokenExpiredException extends AuthenticationException {

    transient private JwtToken token;

    public JwtTokenExpiredException(String msg) {
        super(msg);
    }

    public JwtTokenExpiredException(JwtToken token, String msg, Throwable t) {
        super(msg, t);
        this.token = token;
    }

    public String token() {
        return this.token.getToken();
    }

}
