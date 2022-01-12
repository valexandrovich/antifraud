package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ua.com.solidity.common.Utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Scheduler {
    public static final String KEY_EXCHANGE = "exchange";
    public static final String KEY_SCHEDULE = "schedule";
    public static final String KEY_DATA = "data";

    public static final String MSG_REFRESH = "init";

    public static final String LOG_HANDLED = "handled: task for exchange={} : {}";

    @Autowired
    private Config config;

    private static class Task implements Comparable<Task> {
        public static final int BEFORE = -1;
        public static final int AFTER = 1;
        public static final int EQUAL = 0;

        public final Schedule schedule;
        public final String exchange;
        public final JsonNode data;
        private LocalDateTime datetime = null;
        private boolean mIsActive = true;

        private Task(Schedule schedule, String exchange, JsonNode data) {
            this.schedule = schedule;
            this.exchange = exchange;
            this.data = data;
        }

        public static Task createTask(Schedule schedule, String exchange, JsonNode data) {
            if (schedule == null) return null;
            return new Task(schedule, exchange, data);
        }

        public final synchronized boolean isActive() { return mIsActive && datetime != null; }

        @Override
        public int hashCode() {
            return Objects.hash(exchange, schedule, data);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Task)) return false;
            return compareTo((Task) o) == EQUAL;
        }

        @Override
        public int compareTo(@NonNull Scheduler.Task o) {
            if (o.datetime == null || !o.mIsActive) {
                return BEFORE;
            } else if (datetime == null || mIsActive) {
                return AFTER;
            } else {
                int compareResult = datetime.compareTo(o.datetime);
                if (compareResult == EQUAL) {
                    compareResult = o.exchange.compareTo(exchange);
                }

                return compareResult;
            }
        }

        public final void next(LocalDateTime aDatetime) {
            datetime = schedule.nearest(aDatetime, true);
        }
    }

    private static class SendThread extends Thread {
        public final Scheduler scheduler;
        LocalDateTime now;
        LocalDateTime datetime;

        public SendThread(Scheduler aScheduler) {
            scheduler = aScheduler;
        }

        private boolean exitFromNextTask() {
            long seconds = datetime.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC);
            if (seconds > 0) {
                try {
                    sleep(seconds * 1000);
                } catch (InterruptedException e) {
                    interrupt();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void run() {
            while (!interrupted()) {
                now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
                datetime = scheduler.sendMessages(now);
                if (datetime == null || exitFromNextTask()) break;
            }
        }
    }

    private final List<Task> mTasks = new ArrayList<>();
     private SendThread mSender;

    public final void clear() {
        synchronized (mTasks) {
            stopSenderThread();
            mTasks.clear();
        }
    }

    private void sendMessage(Task task) {
        synchronized (mTasks) {
            Utils.sendRabbitMQMessage(task.exchange, task.data.toString());
        }
        log.info(LOG_HANDLED, task.exchange, task.data);
    }

    public final void refresh() {
        synchronized(mTasks) {
            Utils.sendRabbitMQMessage(config.getInitName(), MSG_REFRESH);
        }
    }

    protected final LocalDateTime sendMessages(LocalDateTime now) {
        synchronized (mTasks) {
            int count;
            do {
                count = 0;
                for (Task task : mTasks) {
                    if (task.isActive() && (task.datetime.isBefore(now) || task.datetime.isEqual(now))) {
                        sendMessage(task);
                        task.next(now);
                        ++count;
                    }
                }
                Collections.sort(mTasks);
            } while (count > 0);

            return mTasks.isEmpty() ? null : mTasks.get(0).datetime;
        }
    }

    private void stopSenderThread() {
        if (mSender != null) {
            mSender.interrupt();
            mSender = null;
        }
    }

    private void startSenderThread() {
        if (mSender == null) {
            mSender = new SendThread(this);
            mSender.start();
        }
    }

    public final void initialize() {
        synchronized (mTasks) {
            stopSenderThread();
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            for (Task task : mTasks) {
                task.datetime = task.schedule.nearest(now, false);
            }
            Collections.sort(mTasks);
            log.info("Invalidate accepted. {} tasks loaded", mTasks.size());
            startSenderThread();
        }
    }

    private void addTask(Schedule schedule, String exchange, JsonNode data) {
        Task task = Task.createTask(schedule, exchange, data);
        if (task != null) mTasks.add(task);
    }

    private void addTask(JsonNode task) {
        Schedule schedule = new Schedule();
        try {
            if (!schedule.assignNode(task.get(KEY_SCHEDULE))) {
                log.warn("Schedule JSON parse error");
                return;
             }
            String exchange = task.get(KEY_EXCHANGE).asText();
            JsonNode data = task.get(KEY_DATA);
            addTask(schedule, exchange, data);
        } catch (Exception e) {
            log.warn("Error Scheduler Task parsing from JSON", e);
        }
    }

    public final void updateTasks(JsonNode arr) {
        synchronized (mTasks) {
            clear();
            if (arr == null) return;

            for (JsonNode obj : arr) {
                if (obj.isObject()) {
                    addTask(obj);
                }
            }
            initialize();
        }
    }

    private Task getTask(String exchange) {
        for (Task task : mTasks) {
            if (task.exchange.equals(exchange)) {
                return task;
            }
        }

        log.warn("Task not found (exchange=\"{}\")", exchange);
        return null;
    }

    public final void setTaskActive(String exchange, boolean isActive) {
        synchronized(mTasks) {
            Task task = getTask(exchange);
            if (task == null) return;
            if (task.mIsActive != isActive) {
                task.mIsActive = isActive;
                Collections.sort(mTasks);
            }
        }
    }

    public final void setTaskActive(@NonNull JsonNode obj, boolean isActive) {
        setTaskActive(obj.get(KEY_EXCHANGE).asText(), isActive);
    }

    public final void taskForceExecute(String topic) {
        synchronized(mTasks) {
            Task task = getTask(topic);
            if (task == null) return;
            sendMessage(task);
        }
    }

    public final void taskForceExecute(@NonNull JsonNode obj) {
        taskForceExecute(obj.get(KEY_EXCHANGE).asText());
    }
}

