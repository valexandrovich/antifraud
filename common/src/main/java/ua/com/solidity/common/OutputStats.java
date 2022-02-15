package ua.com.solidity.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OutputStats {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Group {
        private static final long DELTA = 1000000;
        String fullName;
        String name;
        long totalRowCount = 0;
        long parseErrorCount = 0;
        long insertCount = 0;
        DurationPrinter printer = new DurationPrinter();

        public Group(String source, String name) {
            this.name = name;
            this.fullName = source + "." + name;
        }

        public void clear() {
            totalRowCount = parseErrorCount = insertCount = 0;
        }

        public final void incParseErrorCount(long count) {
            totalRowCount += count;
            parseErrorCount += count;
        }

        public final void incRowCount(long count) {
            totalRowCount += count;
        }

        public final void incInsertCount(long count) {
            insertCount += count;
        }

        public final String getStatsMessage() {
            return Utils.messageFormat("{}: [total: {}/errors: {}({}%)/inserted: {}({}%)] due {}",
                    fullName, totalRowCount, parseErrorCount, getParseErrorPercent(), insertCount, getInsertedPercent(), printer.getDurationString());
        }

        public final double getParseErrorPercent() {
            return totalRowCount == 0 ? 0 : (double) parseErrorCount / totalRowCount * 100;
        }

        public final double getInsertedPercent() {
            return totalRowCount == 0 ? 0 : (double) insertCount / totalRowCount * 100;
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
