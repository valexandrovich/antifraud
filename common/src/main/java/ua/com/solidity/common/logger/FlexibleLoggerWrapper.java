package ua.com.solidity.common.logger;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import java.util.Arrays;
import java.util.List;

public class FlexibleLoggerWrapper implements Logger {
    protected Logger logger;
    final String fqcn;
    protected boolean instanceofLAL;

    private interface ILogThrowable {
        void log(String msg, Throwable t);
    }

    private interface ILogMarkerThrowable {
        void log(Marker marker, String msg, Throwable t);
    }

    private static class ParsedMessage {
        final String msg;
        String option = null;
        boolean isEnabled = true;

        public ParsedMessage(String msg, boolean isDebugEnabled) {
            if (msg.startsWith("$")) {
                int index = msg.indexOf("$", 1);
                if (index > 2) {
                    option = msg.substring(1, index).trim();
                    msg = msg.substring(index + 1);
                    if (!option.isBlank()) {
                        this.isEnabled = isDebugEnabled || LoggerWrapperFactory.isOptionIncluded(option);
                    }
                }
            }
            this.msg = msg;
        }
    }

    @Getter
    private static class LoggerInfo {
        private final ILogThrowable logThrowable;
        private final ILogMarkerThrowable logMarkerThrowable;
        private final Marker marker;
        private String message;
        private Object[] objects = null;
        Throwable throwable = null;
        private final boolean redirected;

        public static LoggerInfo createByArray(ILogThrowable logThrowable, ILogMarkerThrowable logMarkerThrowable, Marker marker, String message, Object[] args) {
            return new LoggerInfo(logThrowable, logMarkerThrowable, marker, message, args);
        }

        public static LoggerInfo create(ILogThrowable logThrowable, ILogMarkerThrowable logMarkerThrowable, Marker marker, String message, Object... args) {
            return new LoggerInfo(logThrowable, logMarkerThrowable, marker, message, args);
        }

        private LoggerInfo(ILogThrowable logThrowable, ILogMarkerThrowable logMarkerThrowable, Marker marker, String message, Object[] args) {
            this.logThrowable = logThrowable;
            this.logMarkerThrowable = logMarkerThrowable;
            this.marker = marker;
            this.redirected = args.length > 0 && args[args.length - 1] instanceof Throwable;
            this.message = message;
            if (this.redirected) {
                initThrowable(args);
            } else {
                this.throwable = null;
                this.message = MessageFormatter.arrayFormat(message, args).getMessage();
                this.objects = args;
            }
        }

        private void initThrowable(Object[] args) {
            throwable = (Throwable) args[args.length - 1];
            if (args.length > 1) {
                message = MessageFormatter.arrayFormat(message, Arrays.copyOf(args, args.length - 1)).getMessage();
            }
            this.objects = null;

            if (marker != null && logMarkerThrowable != null) {
                logMarkerThrowable.log(marker, message, throwable);
                return;
            }

            if (logThrowable != null) {
                logThrowable.log(message, throwable);
            }
        }
    }

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
        return logger.isTraceEnabled();
    }

    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    public void trace(String msg) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isTraceEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, msg, null, null);
            } else {
                this.logger.trace(msg);
            }
        }
    }

    public void trace(String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isTraceEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::trace, this::trace, null, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, info.message, info.objects, null);
                } else {
                    this.logger.trace(format, arg);
                }
            }
        }
    }

    public void trace(String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isTraceEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::trace, this::trace, null, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, info.message, info.objects, null);
                } else {
                    this.logger.trace(format, arg1, arg2);
                }
            }
        }
    }

    public void trace(String format, Object...args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isTraceEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::trace, this::trace, null,  format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 0, info.message, info.objects, null);
                } else {
                    this.logger.trace(format, args);
                }
            }
        }
    }

    public void trace(String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isTraceEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 0, msg, null, t);
            } else {
                this.logger.trace(msg, t);
            }
        }
    }

    public void trace(Marker marker, String msg) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isTraceEnabled(marker) && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 0, msg, null, null);
            } else {
                this.logger.trace(marker, msg);
            }
        }
    }

    public void trace(Marker marker, String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isTraceEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::trace, this::trace, marker, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 0, info.message, info.objects, null);
                } else {
                    this.logger.trace(marker, format, arg);
                }
            }
        }
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.isTraceEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::trace, this::trace, marker, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 0, info.message, info.objects, null);
                } else {
                    this.logger.trace(marker, format, arg1, arg2);
                }
            }
        }
    }

    public void trace(Marker marker, String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.isTraceEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::trace, this::trace, marker, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 0, info.message, info.objects, null);
                } else {
                    this.logger.trace(marker, format, args);
                }
            }
        }
    }

    public void trace(Marker marker, String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.isTraceEnabled(marker) && parsedMessage.isEnabled) {
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
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.isDebugEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 10, msg, null, null);
            } else {
                this.logger.debug(msg);
            }
        }
    }

    public void debug(String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.isDebugEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::debug, this::debug, null, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 10, info.message, info.objects, null);
                } else {
                    this.logger.debug(format, arg);
                }
            }
        }
    }

    public void debug(String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.isDebugEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::debug, this::debug, null, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 10, info.message, info.objects, null);
                } else {
                    this.logger.debug(format, arg1, arg2);
                }
            }
        }
    }

    public void debug(String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.isDebugEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::debug, this::debug, null, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 10, info.message, info.objects, null);
                } else {
                    this.logger.debug(format, args);
                }
            }
        }
    }

    public void debug(String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isDebugEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 10, msg, null, t);
            } else {
                this.logger.debug(msg, t);
            }
        }
    }

    public void debug(Marker marker, String msg) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isDebugEnabled(marker) && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 10, msg, null, null);
            } else {
                this.logger.debug(marker, msg);
            }
        }
    }

    public void debug(Marker marker, String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isDebugEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::debug, this::debug, marker, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 10, info.message, info.objects, null);
                } else {
                    this.logger.debug(marker, format, arg);
                }
            }
        }
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isDebugEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::debug, this::debug, marker, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 10, info.message, info.objects, null);
                } else {
                    this.logger.debug(marker, format, arg1, arg2);
                }
            }
        }
    }

    public void debug(Marker marker, String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isDebugEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::debug, this::debug, marker, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 10, info.message, info.objects, null);
                } else {
                    this.logger.debug(marker, format, args);
                }
            }
        }
    }

    public void debug(Marker marker, String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isDebugEnabled(marker) && parsedMessage.isEnabled) {
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
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isInfoEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 20, msg, null, null);
            } else {
                this.logger.info(msg);
            }
        }
    }

    public void info(String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.isInfoEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::info, this::info, null, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 20, info.message, info.objects, null);
                } else {
                    this.logger.info(format, arg);
                }
            }
        }
    }

    public void info(String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.isInfoEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::info, this::info, null, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 20, info.message, info.objects, null);
                } else {
                    this.logger.info(format, arg1, arg2);
                }
            }
        }
    }

    public void info(String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.isInfoEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::info, this::info, null, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger)this.logger).log(null, this.fqcn, 20, info.message, info.objects, null);
                } else {
                    this.logger.info(format, args);
                }
            }
        }
    }

    public void info(String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.isInfoEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 20, msg, null, t);
            } else {
                this.logger.info(msg, t);
            }
        }
    }

    public void info(Marker marker, String msg) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isInfoEnabled(marker) && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 20, msg, null, null);
            } else {
                this.logger.info(marker, msg);
            }
        }
    }

    public void info(Marker marker, String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isInfoEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::info, this::info, marker, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 20, info.message, info.objects, null);
                } else {
                    this.logger.info(marker, format, arg);
                }
            }
        }
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isInfoEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::info, this::info, marker, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 20, info.message, info.objects, null);
                } else {
                    this.logger.info(marker, format, arg1, arg2);
                }
            }
        }
    }

    public void info(Marker marker, String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (isInfoEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::info, this::info, marker, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 20, info.message, info.objects, null);
                } else {
                    this.logger.info(marker, format, args);
                }
            }
        }
    }

    public void info(Marker marker, String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isInfoEnabled(marker) && parsedMessage.isEnabled) {
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
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (isWarnEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 30, msg, null, null);
            } else {
                this.logger.warn(msg);
            }
        }
    }

    public void warn(String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isWarnEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::warn, this::warn, null, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 30, info.message, info.objects, null);
                } else {
                    this.logger.warn(format, arg);
                }
            }
        }
    }

    public void warn(String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isWarnEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::warn, this::warn, null, format, arg1, 2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 30, info.message, info.objects, null);
                } else {
                    this.logger.warn(format, arg1, arg2);
                }
            }
        }
    }

    public void warn(String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isWarnEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::warn, this::warn, null, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 30, info.message, info.objects, null);
                } else {
                    this.logger.warn(format, args);
                }
            }
        }
    }

    public void warn(String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.logger.isWarnEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 30, msg, null, t);
            } else {
                this.logger.warn(msg, t);
            }
        }
    }

    public void warn(Marker marker, String msg) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.logger.isWarnEnabled(marker) && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 30, msg, null, null);
            } else {
                this.logger.warn(marker, msg);
            }
        }
    }

    public void warn(Marker marker, String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isWarnEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::warn, this::warn, marker, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 30, info.message, info.objects, null);
                } else {
                    this.logger.warn(marker, format, arg);
                }
            }
        }
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isWarnEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::warn, this::warn, marker, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 30, info.message, info.objects, null);
                } else {
                    this.logger.warn(marker, format, arg1, arg2);
                }
            }
        }
    }

    public void warn(Marker marker, String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isWarnEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::warn, this::warn, marker, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 30, info.message, info.objects, null);
                } else {
                    this.logger.warn(marker, format, args);
                }
            }
        }
    }

    public void warn(Marker marker, String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.logger.isWarnEnabled(marker) && parsedMessage.isEnabled) {
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
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.logger.isErrorEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 40, msg, null, null);
            } else {
                this.logger.error(msg);
            }
        }
    }

    public void error(String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isErrorEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::error, this::error, null, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 40, info.message, info.objects, null);
                } else {
                    this.logger.error(format, arg);
                }
            }
        }
    }

    public void error(String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isErrorEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::error, this::error, null, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 40, info.message, info.objects, null);
                } else {
                    this.logger.error(format, arg1, arg2);
                }
            }
        }
    }

    public void error(String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isErrorEnabled() && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::error, this::error, null, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(null, this.fqcn, 40, info.message, info.objects, null);
                } else {
                    this.logger.error(format, args);
                }
            }
        }
    }

    public void error(String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.logger.isErrorEnabled() && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(null, this.fqcn, 40, msg, null, t);
            } else {
                this.logger.error(msg, t);
            }
        }
    }

    public void error(Marker marker, String msg) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.logger.isErrorEnabled(marker) && parsedMessage.isEnabled) {
            if (this.instanceofLAL) {
                ((LocationAwareLogger)this.logger).log(marker, this.fqcn, 40, msg, null, null);
            } else {
                this.logger.error(marker, msg);
            }
        }
    }

    public void error(Marker marker, String format, Object arg) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isErrorEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::error, this::error, marker, format, arg);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 40, info.message, info.objects, null);
                } else {
                    this.logger.error(marker, format, arg);
                }
            }
        }
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isErrorEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.create(this::error, this::error, marker, format, arg1, arg2);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 40, info.message, info.objects, null);
                } else {
                    this.logger.error(marker, format, arg1, arg2);
                }
            }
        }
    }

    public void error(Marker marker, String format, Object... args) {
        ParsedMessage parsedMessage = new ParsedMessage(format, isDebugEnabled());
        format = parsedMessage.msg;

        if (this.logger.isErrorEnabled(marker) && parsedMessage.isEnabled) {
            LoggerInfo info = LoggerInfo.createByArray(this::error, this::error, marker, format, args);
            if (!info.redirected) {
                if (this.instanceofLAL) {
                    ((LocationAwareLogger) this.logger).log(marker, this.fqcn, 40, info.message, info.objects, null);
                } else {
                    this.logger.error(marker, format, args);
                }
            }
        }
    }

    public void error(Marker marker, String msg, Throwable t) {
        ParsedMessage parsedMessage = new ParsedMessage(msg, isDebugEnabled());
        msg = parsedMessage.msg;

        if (this.logger.isErrorEnabled(marker) && parsedMessage.isEnabled) {
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
