package ua.com.solidity.common.logger;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import java.util.List;

public class FlexibleLoggerWrapper implements Logger {
    protected Logger logger;
    final String fqcn;
    protected boolean instanceofLAL;
    
    public FlexibleLoggerWrapper(Logger logger, String fqcn) {
        this.logger = logger;
        this.fqcn = fqcn;
        this.instanceofLAL = logger instanceof LocationAwareLogger;
    }

    private Logger getTerminal() {
        return logger instanceof FlexibleLoggerWrapper ? ((FlexibleLoggerWrapper) logger).getTerminal() : logger;
    }

    public final void rebuildBy(List<Class<? extends FlexibleLoggerWrapper>> wrappers) {
        Logger target = getTerminal();
        if (wrappers != null && !wrappers.isEmpty()) {
            for (var item: wrappers)
                try {
                    target = item.getConstructor(Logger.class, String.class).newInstance(target, item.getName());
                } catch (Exception e) {
                    // nothing
                }
            logger = target;
        }
        this.instanceofLAL = logger instanceof LocationAwareLogger;
    }
    
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }
    
    public boolean isTraceEnabled(Marker marker) {
        return this.logger.isTraceEnabled(marker);
    }
    
    public void trace(String msg) {
        if (this.logger.isTraceEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, msg, null, null);
            } else {
                this.logger.trace(msg);
            }
        }
    }

    public void trace(String format, Object arg) {
        if (this.logger.isTraceEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.trace(format, arg);
            }
        }
    }

    public void trace(String format, Object arg1, Object arg2) {
        if (this.logger.isTraceEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.trace(format, arg1, arg2);
            }
        }
    }

    public void trace(String format, Object... args) {
        if (this.logger.isTraceEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, formattedMessage, args, null);
            } else {
                this.logger.trace(format, args);
            }
        }
    }

    public void trace(String msg, Throwable t) {
        if (this.logger.isTraceEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, msg, null, t);
            } else {
                this.logger.trace(msg, t);
            }
        }
    }

    public void trace(Marker marker, String msg) {
        if (this.logger.isTraceEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 0, msg, null, null);
            } else {
                this.logger.trace(marker, msg);
            }
        }
    }

    public void trace(Marker marker, String format, Object arg) {
        if (this.logger.isTraceEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 0, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.trace(marker, format, arg);
            }
        }
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (this.logger.isTraceEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 0, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.trace(marker, format, arg1, arg2);
            }
        }
    }

    public void trace(Marker marker, String format, Object... args) {
        if (this.logger.isTraceEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 0, formattedMessage, args, null);
            } else {
                this.logger.trace(marker, format, args);
            }
        }
    }

    public void trace(Marker marker, String msg, Throwable t) {
        if (this.logger.isTraceEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 0, msg, null, t);
            } else {
                this.logger.trace(marker, msg, t);
            }
        }
    }

    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    public boolean isDebugEnabled(Marker marker) {
        return this.logger.isDebugEnabled(marker);
    }

    public void debug(String msg) {
        if (this.logger.isDebugEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 10, msg, null, null);
            } else {
                this.logger.debug(msg);
            }
        }
    }

    public void debug(String format, Object arg) {
        if (this.logger.isDebugEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 10, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.debug(format, arg);
            }
        }
    }

    public void debug(String format, Object arg1, Object arg2) {
        if (this.logger.isDebugEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 10, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.debug(format, arg1, arg2);
            }
        }
    }

    public void debug(String format, Object... argArray) {
        if (this.logger.isDebugEnabled()) {
            if (this.instanceofLAL) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 10, ft.getMessage(), ft.getArgArray(), ft.getThrowable());
            } else {
                this.logger.debug(format, argArray);
            }
        }
    }

    public void debug(String msg, Throwable t) {
        if (this.logger.isDebugEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 10, msg, null, t);
            } else {
                this.logger.debug(msg, t);
            }
        }
    }

    public void debug(Marker marker, String msg) {
        if (this.logger.isDebugEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 10, msg, null, null);
            } else {
                this.logger.debug(marker, msg);
            }
        }
    }

    public void debug(Marker marker, String format, Object arg) {
        if (this.logger.isDebugEnabled(marker)) {
            if (this.instanceofLAL) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 10, ft.getMessage(), ft.getArgArray(), ft.getThrowable());
            } else {
                this.logger.debug(marker, format, arg);
            }
        }
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (this.logger.isDebugEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 10, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.debug(marker, format, arg1, arg2);
            }
        }
    }

    public void debug(Marker marker, String format, Object... argArray) {
        if (this.logger.isDebugEnabled(marker)) {
            if (this.instanceofLAL) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 10, ft.getMessage(), argArray, ft.getThrowable());
            } else {
                this.logger.debug(marker, format, argArray);
            }
        }
    }

    public void debug(Marker marker, String msg, Throwable t) {
        if (this.logger.isDebugEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 10, msg, null, t);
            } else {
                this.logger.debug(marker, msg, t);
            }
        }
    }

    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    public boolean isInfoEnabled(Marker marker) {
        return this.logger.isInfoEnabled(marker);
    }

    public void info(String msg) {
        if (this.logger.isInfoEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 20, msg, null, null);
            } else {
                this.logger.info(msg);
            }
        }
    }

    public void info(String format, Object arg) {
        if (this.logger.isInfoEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 20, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.info(format, arg);
            }
        }
    }

    public void info(String format, Object arg1, Object arg2) {
        if (this.logger.isInfoEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 20, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.info(format, arg1, arg2);
            }
        }
    }

    public void info(String format, Object... args) {
        if (this.logger.isInfoEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 20, formattedMessage, args, null);
            } else {
                this.logger.info(format, args);
            }
        }
    }

    public void info(String msg, Throwable t) {
        if (this.logger.isInfoEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 20, msg, null, t);
            } else {
                this.logger.info(msg, t);
            }
        }
    }

    public void info(Marker marker, String msg) {
        if (this.logger.isInfoEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 20, msg, null, null);
            } else {
                this.logger.info(marker, msg);
            }
        }
    }

    public void info(Marker marker, String format, Object arg) {
        if (this.logger.isInfoEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 20, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.info(marker, format, arg);
            }
        }
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (this.logger.isInfoEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 20, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.info(marker, format, arg1, arg2);
            }
        }
    }

    public void info(Marker marker, String format, Object... args) {
        if (this.logger.isInfoEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 20, formattedMessage, args, null);
            } else {
                this.logger.info(marker, format, args);
            }
        }
    }

    public void info(Marker marker, String msg, Throwable t) {
        if (this.logger.isInfoEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 20, msg, null, t);
            } else {
                this.logger.info(marker, msg, t);
            }
        }
    }

    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    public boolean isWarnEnabled(Marker marker) {
        return this.logger.isWarnEnabled(marker);
    }

    public void warn(String msg) {
        if (this.logger.isWarnEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 30, msg, null, null);
            } else {
                this.logger.warn(msg);
            }
        }
    }

    public void warn(String format, Object arg) {
        if (this.logger.isWarnEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 30, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.warn(format, arg);
            }
        }
    }

    public void warn(String format, Object arg1, Object arg2) {
        if (this.logger.isWarnEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 30, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.warn(format, arg1, arg2);
            }
        }
    }

    public void warn(String format, Object... args) {
        if (this.logger.isWarnEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 30, formattedMessage, args, null);
            } else {
                this.logger.warn(format, args);
            }
        }
    }

    public void warn(String msg, Throwable t) {
        if (this.logger.isWarnEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 30, msg, null, t);
            } else {
                this.logger.warn(msg, t);
            }
        }
    }

    public void warn(Marker marker, String msg) {
        if (this.logger.isWarnEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 30, msg, null, null);
            } else {
                this.logger.warn(marker, msg);
            }
        }
    }

    public void warn(Marker marker, String format, Object arg) {
        if (this.logger.isWarnEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 30, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.warn(marker, format, arg);
            }
        }
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (this.logger.isWarnEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 30, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.warn(marker, format, arg1, arg2);
            }
        }
    }

    public void warn(Marker marker, String format, Object... args) {
        if (this.logger.isWarnEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 30, formattedMessage, args, null);
            } else {
                this.logger.warn(marker, format, args);
            }
        }
    }

    public void warn(Marker marker, String msg, Throwable t) {
        if (this.logger.isWarnEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 30, msg, null, t);
            } else {
                this.logger.warn(marker, msg, t);
            }
        }
    }

    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    public boolean isErrorEnabled(Marker marker) {
        return this.logger.isErrorEnabled(marker);
    }

    public void error(String msg) {
        if (this.logger.isErrorEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 40, msg, null, null);
            } else {
                this.logger.error(msg);
            }
        }
    }

    public void error(String format, Object arg) {
        if (this.logger.isErrorEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 40, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.error(format, arg);
            }
        }
    }

    public void error(String format, Object arg1, Object arg2) {
        if (this.logger.isErrorEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 40, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.error(format, arg1, arg2);
            }
        }
    }

    public void error(String format, Object... args) {
        if (this.logger.isErrorEnabled()) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 40, formattedMessage, args, null);
            } else {
                this.logger.error(format, args);
            }
        }
    }

    public void error(String msg, Throwable t) {
        if (this.logger.isErrorEnabled()) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 40, msg, null, t);
            } else {
                this.logger.error(msg, t);
            }
        }
    }

    public void error(Marker marker, String msg) {
        if (this.logger.isErrorEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 40, msg, null, null);
            } else {
                this.logger.error(marker, msg);
            }
        }
    }

    public void error(Marker marker, String format, Object arg) {
        if (this.logger.isErrorEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 40, formattedMessage, new Object[]{arg}, null);
            } else {
                this.logger.error(marker, format, arg);
            }
        }
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (this.logger.isErrorEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 40, formattedMessage, new Object[]{arg1, arg2}, null);
            } else {
                this.logger.error(marker, format, arg1, arg2);
            }
        }
    }

    public void error(Marker marker, String format, Object... args) {
        if (this.logger.isErrorEnabled(marker)) {
            if (this.instanceofLAL) {
                String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 40, formattedMessage, args, null);
            } else {
                this.logger.error(marker, format, args);
            }
        }
    }

    public void error(Marker marker, String msg, Throwable t) {
        if (this.logger.isErrorEnabled(marker)) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 40, msg, null, t);
            } else {
                this.logger.error(marker, msg, t);
            }
        }
    }

    public String getName() {
        return this.logger.getName();
    }
}

