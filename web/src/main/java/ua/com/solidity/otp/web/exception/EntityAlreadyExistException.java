package ua.com.solidity.otp.web.exception;

public class EntityAlreadyExistException extends RuntimeException {

    public EntityAlreadyExistException(Class type, Long id) {
        super(type.getSimpleName() + " with id " + id + " is already exists.");
    }

    public EntityAlreadyExistException(Class type, String name) {
        super(type.getSimpleName() + " with name " + name + " is already exists.");
    }

}
