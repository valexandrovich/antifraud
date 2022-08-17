package ua.com.solidity.common.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.CustomLog;

import java.time.Instant;
import java.util.*;

@CustomLog
@SuppressWarnings("unused")
public class Metric {
    public static final Map<String, Metric> items = new HashMap<>();
    private double value = 0;
    private double startValue = 0;
    private double minValue = 0;
    private double maxValue = 0;
    private double accumulator = 0;
    private long startMs = 0;
    private long currentMs = 0;
    private long maxValueMs = 0;
    private long minValueMs = 0;
    private final MetricTuple metricTuple;

    public static Metric create(String name) {
        synchronized(items) {
            Metric res = items.getOrDefault(name, null);
            if (res == null) {
                res = new Metric(name);
                items.put(name, res);
            }
            return res;
        }
    }

    private Metric(String name) {
        metricTuple = new MetricTuple(name);
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
        startMs = currentMs = minValueMs = maxValueMs = start.toEpochMilli();
        startValue = minValue = maxValue = 0;
    }

    private void putValue(double value, long current) {
        if (startMs <= 0) return;

        double priorValue = this.value;

        if (value < minValue) {
            minValue = value;
            minValueMs = current;
        }

        if (value > maxValue) {
            maxValue = value;
            maxValueMs = current;
        }

        double accumValue = value + priorValue;
        long deltaMs = current - currentMs;

        if (accumValue != 0 && deltaMs > 0) {
            accumulator += accumValue * (((double) deltaMs) / 2f);
        }
        currentMs = current;
        this.value = value;
        log.info("$monitor${} put: {} at {}.", metricTuple.getName(), value, current);
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
    public synchronized MetricTuple doFlush() {
        long finishMs = Instant.now().toEpochMilli();
        putValue(value, finishMs);
        long deltaMs = finishMs - startMs;
        metricTuple.assign(startMs, finishMs, startValue, value, minValue, maxValue, deltaMs > 0 ? accumulator / deltaMs : 0);
        minValue = maxValue = startValue = value;
        minValueMs = maxValueMs = startMs = finishMs;
        accumulator = 0;
        log.info("$monitor${} flush: (start: {}, finish: {}, min: {}, max: {}, avg: {}), startTime:{}, finishTime: {}.", metricTuple.getName(),
                metricTuple.getStartValue(), metricTuple.getFinishValue(), metricTuple.getMin(), metricTuple.getMax(), metricTuple.getAvg(),
                metricTuple.getStart(), metricTuple.getFinish());
        return metricTuple;
    }

    public synchronized void release() { // too more synchronized, is a trouble?
        startMs = 0;
        synchronized(items) {
            items.remove(metricTuple.getName());
        }
    }

    public static ObjectNode flush() {
        ObjectNode res = null;
        synchronized(items) {
            if (!items.isEmpty()) {
                res = JsonNodeFactory.instance.objectNode();
                for (var item : items.entrySet()) {
                    res.set(item.getKey(), ServiceMonitor.mapper.valueToTree(item.getValue().doFlush()));
                }
            }
        }
        return res == null || !res.isObject() ? null : res;
    }
}
