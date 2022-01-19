package ua.com.solidity.otp.web.security.exception;

import org.springframework.security.authentication.AuthenticationServiceException;


public class CRUDMethodNotSupportedException extends AuthenticationServiceException {

    public CRUDMethodNotSupportedException(String msg) {
        super(msg);
    }

}
