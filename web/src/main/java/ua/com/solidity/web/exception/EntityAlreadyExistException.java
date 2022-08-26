package ua.com.solidity.web.exception;

public class EntityAlreadyExistException extends RuntimeException {

    public EntityAlreadyExistException(String message) {
        super(message);
    }

    public EntityAlreadyExistException(Class<?> type, Long id) {
        super(type.getSimpleName() + " with id " + id + " is already exists.");
    }

    public EntityAlreadyExistException(Class<?> type, String name) {
        super(type.getSimpleName() + " with name " + name + " is already exists.");
    }

}
