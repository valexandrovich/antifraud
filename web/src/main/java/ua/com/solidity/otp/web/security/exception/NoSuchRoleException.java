package ua.com.solidity.otp.web.security.exception;

public class NoSuchRoleException extends RuntimeException{

    public NoSuchRoleException(String role) {
        super("No such role in service like: " + role);
    }
}
