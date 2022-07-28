package ua.com.solidity.common.pgsql;

import lombok.CustomLog;


@CustomLog
public enum SQLFlushMode {
    PREPARED_STATEMENT_BATCH,
    COPY_BUFFER;

    private static final String PREPARED_STATEMENT_BATCH_NAME = "statement";
    private static final String COPY_BUFFER_NAME = "copy";

    public static SQLFlushMode parse(String value) {
        switch (value) {
            case PREPARED_STATEMENT_BATCH_NAME:
                return PREPARED_STATEMENT_BATCH;
            case COPY_BUFFER_NAME:
                return COPY_BUFFER;
            default:
                log.warn("Table flush mode is invalid ({}). Prepared statement mode used.", value);
                return PREPARED_STATEMENT_BATCH;
        }
    }
}
