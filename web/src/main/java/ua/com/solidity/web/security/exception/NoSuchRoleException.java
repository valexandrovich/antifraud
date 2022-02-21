package ua.com.solidity.web.security.exception;

public class NoSuchRoleException extends RuntimeException{

    public NoSuchRoleException(String role) {
        super("No such role in service like: " + role);
    }

    public NoSuchRoleException() {
        super("No such role in service");
    }
}
