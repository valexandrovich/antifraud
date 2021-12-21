package ua.com.solidity.common;

import java.time.Duration;
import java.time.Instant;

public class DurationPrinter {
    Instant start;
    Instant finish = null;

    public DurationPrinter() {
        reset();
    }

    public final void reset() {
        start = Instant.now();
    }

    public final void stop() {
        finish = Instant.now();
    }

    private void pushDuration(StringBuilder builder, long value, String digit) {
        if (value > 0 || builder.length() > 0) {
            if (builder.length() > 0) builder.append(" ");
            builder.append(value);
            builder.append(digit);
        }
    }

    public String getDurationString() {
        Instant current = finish == null ? Instant.now() : finish;
        StringBuilder builder = new StringBuilder();
        pushDuration(builder, Duration.between(start, current).toDays(), "d");
        pushDuration(builder, Duration.between(start, current).toHoursPart(), "h");
        pushDuration(builder, Duration.between(start, current).toMinutesPart(), "m");
        if (builder.length() > 0) builder.append(" ");
        builder.append(Duration.between(start, current).toSecondsPart());
        long milliseconds = Duration.between(start, current).toMillisPart();
        builder.append(".");
        builder.append(String.format("%3ds", milliseconds));
        return builder.toString();
    }
}
