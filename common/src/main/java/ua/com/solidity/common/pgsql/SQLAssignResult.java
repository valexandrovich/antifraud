package ua.com.solidity.common.pgsql;

public enum SQLAssignResult {
    NORMAL(null),
    NULL_NOT_ALLOWED("Null not allowed."),
    LENGTH_ERROR ("Length is too large."),
    EXCEPTION(null);

    private final String message;

    SQLAssignResult(String message) {
        this.message = message;
    }

    public final String getMessage() {
        return this.message;
    }
}
