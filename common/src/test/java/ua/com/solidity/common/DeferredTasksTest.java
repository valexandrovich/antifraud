package ua.com.solidity.common;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeferredTasksTest {
    private static final List<String> WaitingResult = Arrays.asList("Hello", "World", "!");
    private final List<String> target = new ArrayList<>();

    private static class TestTask extends DeferredTask {
        private final List<String> target;
        private final DeferredAction action;
        private final String ident;
        private final String data;

        public TestTask(List<String> target, DeferredAction action, String ident, String data) {
            this.target = target;
            this.action = action;
            this.ident = ident;
            this.data = data;
        }

        @Override
        public DeferredAction compareWith(DeferredTask task) {
            TestTask testTask = getClass().cast(task);
            if (testTask == null) return DeferredAction.IGNORE;
            if (ident.equals(testTask.ident)) {
                return testTask.action;
            }
            return DeferredAction.APPEND;
        }

        @Override
        public void execute() {
            target.add(this.data);
        }
    }

    private void await() {
        Instant instant = Instant.now();
        Instant waitingFor = instant.plus(300, ChronoUnit.MILLIS);
        while (instant.compareTo(waitingFor) < 0) {
            instant = Instant.now();
        }
    }

    @Test
    void firstTest() {
        DeferredTasks tasks = new DeferredTasks(false, 200);
        tasks.append(new TestTask(target, DeferredAction.APPEND, "id", "hello"));
        tasks.append(new TestTask(target, DeferredAction.APPEND, "id2", "World"));
        tasks.append(new TestTask(target, DeferredAction.REPLACE, "id", "Hello"));
        await();
        tasks.append(new TestTask(target, DeferredAction.APPEND, "id", "!"));
        tasks.waitForExecution();
        assertThat(target).isEqualTo(WaitingResult);
    }
}
