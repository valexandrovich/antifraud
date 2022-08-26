package ua.com.solidity.web.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    private static final String COULD_NOT_FIND = "Could not find ";
    private static final String WITH_ID = " with id ";
    private static final String WITH_NAME = " with name ";

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Class<?> type, UUID uuid) {
        super(COULD_NOT_FIND + type.getSimpleName() + WITH_ID + uuid);
    }

    public EntityNotFoundException(Class<?> type, String name) {
        super(COULD_NOT_FIND + type.getSimpleName() + WITH_NAME + name);
    }

    public EntityNotFoundException(Class<?> type, Long id) {
        super(COULD_NOT_FIND + type.getSimpleName() + WITH_ID + id);
    }

    public EntityNotFoundException(Class<?> type, Integer id) {
        super(COULD_NOT_FIND + type.getSimpleName() + WITH_ID + id);
    }
}
