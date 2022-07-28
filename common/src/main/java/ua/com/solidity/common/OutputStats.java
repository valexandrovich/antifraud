package ua.com.solidity.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class OutputStats {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Group {
        private static final DecimalFormat percentFormat = new DecimalFormat("0.000");
        String fullName;
        String name;
        long totalRowCount = 0;
        long parseErrorCount = 0;
        long badObjectCount = 0;
        long objectErrorCount = 0;

        DurationPrinter printer = new DurationPrinter();

        public Group(String source, String name) {
            this.name = name;
            this.fullName = source + "." + name;
        }

        public void clear() {
            totalRowCount = parseErrorCount = badObjectCount = objectErrorCount = 0;
        }

        @SuppressWarnings("unused")
        public final void incParseErrorCount(long count) {
            totalRowCount += count;
            parseErrorCount += count;
        }

        @SuppressWarnings("unused")
        public final void incRowCount(long count) {
            totalRowCount += count;
        }

        public final long getInsertCount() {
            return totalRowCount - parseErrorCount - badObjectCount;
        }

        public final String getStatsMessage() {
            return Utils.messageFormat("{}: [total: {}/parse errors: {}({})/insert errors: {}({})/inserted: {}({})] due {}",
                    fullName, totalRowCount, parseErrorCount, formatPercent(getParseErrorPercent()),
                    badObjectCount, formatPercent(getInsertErrorPercent()),
                    getInsertCount(), formatPercent(getInsertedPercent()), printer.getDurationString());
        }

        private String formatPercent(double percent) {
            return String.format("%.3f%%", percent);
        }

        public final double getParseErrorPercent() {
            return totalRowCount == 0 ? 0 : (double) parseErrorCount / totalRowCount * 100;
        }

        public final double getInsertErrorPercent() {
            return totalRowCount == 0 ? 0 : (double) badObjectCount/ totalRowCount * 100;
        }

        public final double getInsertedPercent() {
            return totalRowCount == 0 ? 0 : (double) (getInsertCount()) / totalRowCount * 100;
        }
    }

    public final Map<String, Group> items = new HashMap<>();
    public final String source;

    public OutputStats(String source) {
        this.source = source;
    }

    public final Group getGroup(String name) {
        if (items.containsKey(name)) return items.get(name);
        Group group = new Group(source, name);
        items.put(name, group);
        return group;
    }
}
