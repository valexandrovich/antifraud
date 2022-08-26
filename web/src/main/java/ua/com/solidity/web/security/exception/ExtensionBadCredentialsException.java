package ua.com.solidity.web.security.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class ExtensionBadCredentialsException extends BadCredentialsException {

    public ExtensionBadCredentialsException(Class<?> type, Long id) {
        super("Could not find " + type.getSimpleName() + " with id " + id);
    }

    public ExtensionBadCredentialsException(String login) {
        super("Користувача з логіном " + login + " не існує.");
    }
}
