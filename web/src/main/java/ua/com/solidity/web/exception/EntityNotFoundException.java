package ua.com.solidity.web.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class type, UUID uuid) {
        super("Could not find " + type.getSimpleName() + " with id " + uuid);
    }

    public EntityNotFoundException(Class type, String name) {
        super("Could not find " + type.getSimpleName() + " with name " + name);
    }

}
