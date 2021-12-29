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
        String name;
        long totalRowCount = 0;
        long parseErrorCount = 0;
        long insertCount = 0;
        long insertIgnoreCount = 0;
        long insertErrorCount = 0;
        long insertErrorInfoCount = 0;
        long lastTotalRowCount = 0;
        public Group(String name) {
            this.name = name;
        }

        public void clear() {
            totalRowCount = parseErrorCount = insertCount = insertIgnoreCount = insertErrorCount = 0;
        }

        public final void incTotalRowCount() {
            ++totalRowCount;
            if (totalRowCount >= lastTotalRowCount + 100000) {
                lastTotalRowCount = (totalRowCount / 100000) * 100000;
                log.info("...group {} rows handled: {}", name, lastTotalRowCount);
            }
        }

        public final void incParseErrorCount(int count) {
            parseErrorCount += count;
        }

        public final void incInsertCount(int count) {
            insertCount += count;
        }

        public final void incInsertIgnoreCount(int count) {
            insertIgnoreCount += count;
        }

        public final void incInsertErrorCount(int count) {
            insertErrorCount += count;
        }

        public final void incInsertErrorInfoCount(int count) {
            insertErrorInfoCount += count;
        }

        public final double getInsertedPercent() {
            return totalRowCount == 0 ? 0 : (double) insertCount / totalRowCount * 100;
        }

        public final double getIgnoredPercent() {
            return totalRowCount == 0 ? 0 : (double) insertIgnoreCount / totalRowCount * 100;
        }

        public final long getTotalErrorCount() {
            return insertErrorCount + insertErrorInfoCount;
        }

        public final double getErrorHandledPercent() {
            long totalErrorCount = getTotalErrorCount();
            return parseErrorCount == 0 ? 100 : (double) totalErrorCount / parseErrorCount;
        }

        @SuppressWarnings("unused")
        public final double getErrorNotHandledPercent() {
            return 100 - getErrorHandledPercent();
        }

        public final double getHandledPercent() {
            return totalRowCount == 0 ? 0 : (double) (insertCount + insertIgnoreCount) / totalRowCount * 100;
        }
    }

    public final Map<String, Group> items = new HashMap<>();

    public final Group getSource(String name) {
        if (items.containsKey(name)) return items.get(name);
        Group group = new Group(name);
        items.put(name, group);
        return group;
    }
}
