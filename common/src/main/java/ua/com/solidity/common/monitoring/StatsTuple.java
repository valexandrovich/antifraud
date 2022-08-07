package ua.com.solidity.common.monitoring;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("unused")
public class StatsTuple {
    public static final int INDEX_START = 0;
    public static final int INDEX_FINISH = 1;
    public static final int INDEX_MIN = 2;
    public static final int INDEX_MAX = 3;
    public static final int INDEX_AVG = 4;

    private String name;
    private double[] values = new double[INDEX_AVG + 1];
    private long start;
    private long finish;

    StatsTuple(String name) {
        this.name = name;
        clear();
    }

    StatsTuple(String name, long startMs, long finishMs, double start, double finish, double min, double max, double avg) {
        this(name);
        assign(startMs, finishMs, start, finish, min, max, avg);
    }

    public final void clear() {
        start = finish = 0;
        Arrays.fill(values, 0);
    }

    public final void assign(long startMs, long finishMs, double start, double finish, double min, double max, double avg) {
        this.values[0] = start;
        this.values[1] = finish;
        this.values[2] = min;
        this.values[3] = max;
        this.values[4] = avg;
        this.start = startMs;
        this.finish = finishMs;
    }

    public final double getStartValue() {
        return values[INDEX_START];
    }

    public final double getFinishValue() {
        return values[INDEX_FINISH];
    }

    public final double getMin() {
        return values[INDEX_MIN];
    }

    public final double getMax() {
        return values[INDEX_MAX];
    }

    public final double getAvg() {
        return values[INDEX_AVG];
    }
}
