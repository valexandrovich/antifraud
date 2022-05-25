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

    private String getExceptionMessage(String msg, Throwable t) {
        FormattingTuple tuple = MessageFormatter.arrayFormat(EXCEPTION_FORMAT, new Object[] {msg, t.getClass().getName(), t.getMessage()});
        return tuple.getMessage();
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (!isDebugEnabled()) {
            super.trace(getExceptionMessage(msg, t));
        } else {
            super.trace(msg, t);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (!isDebugEnabled()) {
            super.trace(marker, getExceptionMessage(msg, t));
        } else {
            super.trace(marker, msg, t);
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (!isDebugEnabled()) {
            super.info(msg, getExceptionMessage(msg, t));
        } else {
            super.info(msg, t);
        }
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (!isDebugEnabled()) {
            super.info(marker, getExceptionMessage(msg, t));
        } else {
            super.info(msg, t);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (!isDebugEnabled()) {
            super.warn(getExceptionMessage(msg, t));
        } else {
            super.warn(msg, t);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (!isDebugEnabled()) {
            super.warn(marker, getExceptionMessage(msg, t));
        } else {
            super.warn(msg, t);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (!isDebugEnabled()) {
            super.error(getExceptionMessage(msg, t));
        } else {
            super.error(msg, t);
        }
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (!isDebugEnabled()) {
            super.error(marker, getExceptionMessage(msg, t));
        } else {
            super.error(marker, msg, t);
        }
    }
}
