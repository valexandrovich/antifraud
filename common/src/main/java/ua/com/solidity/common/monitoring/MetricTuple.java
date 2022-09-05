package ua.com.solidity.common.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("unused")
public class MetricTuple {
    public static final int INDEX_START = 0;
    public static final int INDEX_FINISH = 1;
    public static final int INDEX_MIN = 2;
    public static final int INDEX_MAX = 3;
    public static final int INDEX_AVG = 4;
    @JsonIgnore
    private String name;
    private double[] values = new double[INDEX_AVG + 1];
    private long start;
    private long finish;

    MetricTuple(String name) {
        this.name = name;
        clear();
    }

    MetricTuple(String name, long startMs, long finishMs, double...args) {
        this(name); // double start, double finish, double min, double max, double avg
        assign(startMs, finishMs,
                getValueAt(args, INDEX_START), getValueAt(args, INDEX_FINISH),
                getValueAt(args, INDEX_MIN), getValueAt(args, INDEX_MAX), getValueAt(args, INDEX_AVG));
    }

    private double getValueAt(double[] data, int index) {
        return data.length > index ? data[index] : 0d;
    }

    public final void clear() {
        start = finish = 0;
        Arrays.fill(values, 0);
    }

    public final void assign(long startMs, long finishMs, double start, double finish, double min, double max, double avg) {
        this.values[INDEX_START] = start;
        this.values[INDEX_FINISH] = finish;
        this.values[INDEX_MIN] = min;
        this.values[INDEX_MAX] = max;
        this.values[INDEX_AVG] = avg;
        this.start = startMs;
        this.finish = finishMs;
    }

    @JsonIgnore
    public final double getStartValue() {
        return values[INDEX_START];
    }
    @JsonIgnore
    public final double getFinishValue() {
        return values[INDEX_FINISH];
    }
    @JsonIgnore
    public final double getMin() {
        return values[INDEX_MIN];
    }
    @JsonIgnore
    public final double getMax() {
        return values[INDEX_MAX];
    }
    @JsonIgnore
    public final double getAvg() {
        return values[INDEX_AVG];
    }
}
