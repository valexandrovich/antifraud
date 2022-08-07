package ua.com.solidity.common.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.CustomLog;

import java.time.Instant;
import java.util.*;

@CustomLog
@SuppressWarnings("unused")
public class Statistic {
    public static final Map<String, Statistic> items = new HashMap<>();
    private double priorValue = 0;
    private double value = 0;
    private double startValue = 0;
    private double minValue = 0;
    private double maxValue = 0;
    private double accumulator = 0;
    private long startMs = 0;
    private long priorMs = 0;
    private long maxValueMs = 0;
    private long minValueMs = 0;
    private final StatsTuple statsTuple;

    public static Statistic create(String name) {
        synchronized(items) {
            Statistic res = items.getOrDefault(name, null);
            if (res == null) {
                res = new Statistic(name);
                items.put(name, res);
            }
            return res;
        }
    }

    private Statistic(String name) {
        statsTuple = new StatsTuple(name);
    }

    public static void initialize(Instant start) {
        synchronized(items) {
            for (var item : items.entrySet()) {
                item.getValue().initializeItem(start);
            }
        }
    }

    final void initializeItem(Instant start) {
        if (startMs > 0) return;
        startMs = priorMs = minValueMs = maxValueMs = start.toEpochMilli();
        startValue = priorValue = minValue = maxValue = 0;
    }

    private void putValue(double value, long current) {
        if (startMs < 0) return;
        if (value < minValue) {
            minValue = value;
            minValueMs = current;
        }

        if (value > maxValue) {
            maxValue = value;
            maxValueMs = current;
        }

        double accumValue = value + priorValue;
        long deltaMs = current - priorMs;

        if (accumValue != 0 && deltaMs > 0) {
            accumulator += accumValue * (((double) deltaMs) / 2e6);
        }
        priorMs = current;
        priorValue = this.value;
        this.value = value;
        log.info("$monitor${} put: {} at {}.", statsTuple.getName(), value, current);
    }

    @JsonIgnore
    private double getRelativePos(long currentMs, long deltaMs) {
        long offsetMs = currentMs - startMs;
        if (offsetMs <= 0) return 0;
        if (deltaMs <= 0) {
            return 1;
        }
        return (double) offsetMs / (double) deltaMs;
    }

    @JsonIgnore
    public synchronized void putValue(double value) {
        putValue(value, Instant.now().toEpochMilli());
    }

    @JsonIgnore
    public synchronized StatsTuple doFlush() {
        long finishMs = Instant.now().toEpochMilli();
        long deltaMs = finishMs - startMs;
        statsTuple.assign(startMs, finishMs, startValue, value, minValue, maxValue, deltaMs > 0 ? accumulator / deltaMs : 0);
        minValue = maxValue = startValue = value;
        minValueMs = maxValueMs = startMs = finishMs;
        accumulator = 0;
        log.info("$monitor${} flush: (start: {}, finish: {}, min: {}, max: {}, avg: {}), startTime:{}, finishTime: {}.", statsTuple.getName(),
                statsTuple.getStartValue(), statsTuple.getFinishValue(), statsTuple.getMin(), statsTuple.getMax(), statsTuple.getAvg(),
                statsTuple.getStart(), statsTuple.getFinish());
        return statsTuple;
    }

    public synchronized void release() { // too more synchronized, is a trouble?
        startMs = 0;
        synchronized(items) {
            items.remove(statsTuple.getName());
        }
    }

    public static ObjectNode flush() {
        JsonNode res;
        synchronized(items) {
            res = items.isEmpty() ? null : ServiceMonitor.mapper.valueToTree(items);
        }
        return res == null || !res.isObject() ? null : ((ObjectNode) res);
    }
}
