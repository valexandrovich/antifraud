package ua.com.solidity.web.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Class type, UUID uuid) {
        super("Could not find " + type.getSimpleName() + " with id " + uuid);
    }

    public EntityNotFoundException(Class type, String name) {
        super("Could not find " + type.getSimpleName() + " with name " + name);
    }

    public EntityNotFoundException(Class type, Long id) {
        super("Could not find " + type.getSimpleName() + " with id " + id);
    }

    public EntityNotFoundException(Class type, Integer id) {
        super("Could not find " + type.getSimpleName() + " with id " + id);
    }
}
