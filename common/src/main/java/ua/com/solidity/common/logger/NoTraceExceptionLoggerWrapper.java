package ua.com.solidity.common.logger;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class NoTraceExceptionLoggerWrapper extends FlexibleLoggerWrapper {
    private static final String EXCEPTION_FORMAT = "{} ({}: {}).";

    public NoTraceExceptionLoggerWrapper(Logger logger, String fqcn) {
        super(logger, fqcn);
    }

    private String getMessage(String msg) {
        return msg.startsWith("$$") ? msg.substring(2) : msg;
    }

    private boolean isStackTraceEnabled(String msg) {
        return msg.startsWith("$$");
    }

    private String getExceptionMessage(String msg, Throwable t) {
        t = t.getCause() != null ? t.getCause() : t;
        FormattingTuple tuple = MessageFormatter.arrayFormat(EXCEPTION_FORMAT, new Object[] {getMessage(msg), t.getClass().getName(), t.getMessage()});
        return tuple.getMessage();
    }

    private boolean isSoftMode(String msg) {
        return !isDebugEnabled() && !isStackTraceEnabled(msg);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isSoftMode(msg)) {
            super.trace(getExceptionMessage(msg, t));
        } else {
            super.trace(getMessage(msg), t);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (isSoftMode(msg)) {
            super.trace(marker, getExceptionMessage(msg, t));
        } else {
            super.trace(marker, getMessage(msg), t);
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isSoftMode(msg)) {
            super.info(msg, getExceptionMessage(msg, t));
        } else {
            super.info(getMessage(msg), t);
        }
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (isSoftMode(msg)) {
            super.info(marker, getExceptionMessage(msg, t));
        } else {
            super.info(getMessage(msg), t);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isSoftMode(msg)) {
            super.warn(getExceptionMessage(msg, t));
        } else {
            super.warn(getMessage(msg), t);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (isSoftMode(msg)) {
            super.warn(marker, getExceptionMessage(msg, t));
        } else {
            super.warn(getMessage(msg), t);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isSoftMode(msg)) {
            super.error(getExceptionMessage(msg, t));
        } else {
            super.error(getMessage(msg), t);
        }
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (isSoftMode(msg)) {
            super.error(marker, getExceptionMessage(msg, t));
        } else {
            super.error(marker, getMessage(msg), t);
        }
    }
}
