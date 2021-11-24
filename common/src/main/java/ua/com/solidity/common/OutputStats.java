package ua.com.solidity.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class OutputStats {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Group {
        String name;
        long totalRowCount = 0;
        long parseErrorCount = 0;
        long insertCount = 0;
        long insertErrorCount = 0;
        long insertErrorInfoCount = 0;
        public Group(String name) {
            this.name = name;
        }

        public void clear() {
            totalRowCount = parseErrorCount = insertCount = insertErrorCount = 0;
        }

        public final void incTotalRowCount() {
            ++totalRowCount;
        }

        public final void incParseErrorCount() {
            ++parseErrorCount;
        }

        public final void incInsertCount() {
            ++insertCount;
        }

        public final void incInsertErrorCount() {
            ++insertCount;
        }

        public final void incInsertErrorInfoCount() {
            ++insertErrorInfoCount;
        }

        public final double getHandledPercent() {
            return totalRowCount == 0 ? 0 : (double) insertCount / totalRowCount * 100;
        }

        public final double getErrorHandledPercent() {
            return parseErrorCount == 0 ? 100 : (double) insertErrorInfoCount / parseErrorCount * 100;
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
