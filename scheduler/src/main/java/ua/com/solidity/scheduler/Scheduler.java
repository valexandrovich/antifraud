package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.SchedulerEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.LocalDateTime.now;

@Slf4j
public class Scheduler {
    public static final String KEY_EXCHANGE = "exchange";
    public static final String KEY_SCHEDULE = "schedule";
    public static final String KEY_DATA = "data";
    public static final String KEY_SLEEP_MS = "sleep_ms";

    public static final String MSG_INIT = "init";

    public static final String LOG_HANDLED = "handled: {} <= {}";

    @Autowired
    private Config config;
    @Autowired
    private String schedulerInitTasks;

    private static class Task implements Comparable<Task> {
        public static final int BEFORE = -1;
        public static final int AFTER = 1;
        public static final int EQUAL = 0;

        public final Schedule schedule;
        public final String exchange;
        public final JsonNode data;
        private LocalDateTime datetime = null;

        private Task(Schedule schedule, String exchange, JsonNode data) {
            this.schedule = schedule;
            this.exchange = exchange;
            this.data = data;
        }

        private Task(long millis, String exchange, JsonNode data) {
            schedule = null;
            this.exchange = exchange;
            this.data = data;
            this.datetime = now().plus(millis, ChronoUnit.MILLIS);
        }

        public static Task createTask(Schedule schedule, String exchange, JsonNode data) {
            if (schedule == null) return null;
            return new Task(schedule, exchange, data);
        }

        public static Task createTemporaryTask(String exchange, JsonNode data, long minutes) {
            return new Task(minutes, exchange, data);
        }

        public final synchronized boolean isTemporary() {
            return schedule == null;
        }

        public final synchronized boolean removeNeeded() {
            return isTemporary() && datetime == null;
        }

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
            if (o.datetime == null) {
                return BEFORE;
            } else if (datetime == null) {
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
            if (schedule == null) {
                datetime = null;
            } else {
                datetime = schedule.nearest(aDatetime, true);
            }
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
                now = now().truncatedTo(ChronoUnit.MINUTES);
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

    private void doSendMessage(String exchange, String message) {
        Utils.sendRabbitMQMessage(exchange, message);
        log.info(LOG_HANDLED, exchange, message);
    }

    private void sendMessage(Task task) {
        synchronized (mTasks) {
            doSendMessage(task.exchange, task.data.toString());
        }
    }

    private void refreshBySendMessage() {
        clear();
        Utils.sendRabbitMQMessage(config.getInitName(), MSG_INIT);
    }

    private void refreshByLoadingInitFile() {
        updateTasks(Utils.getJsonNode(schedulerInitTasks), "initTasks");
    }

    private void logTasks() {
        if (mTasks.isEmpty()) {
            log.info("No tasks found from DB.");
        } else {
            log.info("Loaded {} tasks from DB.", mTasks.size());
        }
    }

    private void refreshByDatabaseRequest() {
        List<SchedulerEntity> list = SchedulerEntity.getAll();
        clear();
        if (list != null) {
            for (SchedulerEntity entity : list) {
                if (entity.getSchedule() == null) {
                    doSendMessage(entity.getExchange(), entity.getData().toString());
                } else {
                    Schedule schedule = new Schedule();
                    try {
                        if (!schedule.assignNode(entity.getSchedule())) {
                            log.warn("Schedule JSON parse error.");
                            continue;
                        }
                        addTask(schedule, entity.getExchange(), entity.getData());
                    } catch (Exception e) {
                        log.warn("Error on add task.", e);
                    }
                }
            }
        }
        initialize();
        logTasks();
    }

    public final void refresh() {
        synchronized(mTasks) {
            if (config.getInit().equals("test")) {
                refreshBySendMessage();
            } else if (config.getInit().equals("debug")) {
                refreshByLoadingInitFile();
            } else {
                refreshByDatabaseRequest();
            }
        }
    }

    protected final LocalDateTime sendMessages(LocalDateTime now) {
        synchronized (mTasks) {
            int count;
            do {
                count = 0;
                for (Task task : mTasks) {
                    if ((task.datetime.isBefore(now) || task.datetime.isEqual(now))) {
                        sendMessage(task);
                        task.next(now);
                        ++count;
                    }
                }
                mTasks.removeIf(Task::removeNeeded);
                Collections.sort(mTasks);
            } while (count > 0);

            return mTasks.isEmpty() ? null : mTasks.get(0).datetime;
        }
    }

    private void stopSenderThread() {
        if (mSender != null) {
            mSender.interrupt();
            try {
                mSender.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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
            LocalDateTime now = now().truncatedTo(ChronoUnit.MINUTES);
            for (Task task : mTasks) {
                if (task.schedule != null) {
                    task.datetime = task.schedule.nearest(now, false);
                }
            }
            Collections.sort(mTasks);
            startSenderThread();
        }
    }

    private boolean addTemporaryTask(long millis, String exchange, JsonNode data) {
        if (exchange == null) return false;
        if (millis <= 0) {
            doSendMessage(exchange, data != null ? data.toString() : "");
            return false;
        } else {
            synchronized (mTasks) {
                mTasks.add(Task.createTemporaryTask(exchange, data, millis));
            }
        }
        return true;
    }

    private boolean addTemporaryTask(JsonNode task) {
        String exchange = task.hasNonNull(KEY_EXCHANGE) ? task.get(KEY_EXCHANGE).asText(null) : null;
        JsonNode data = task.hasNonNull(KEY_DATA) ? task.get(KEY_DATA) : null;
        long millis = task.hasNonNull(KEY_SLEEP_MS) ? task.get(KEY_SLEEP_MS).asLong(0) : 0;
        return addTemporaryTask(millis, exchange, data);
    }

    private boolean addTask(Schedule schedule, String exchange, JsonNode data) {
        Task task = Task.createTask(schedule, exchange, data);
        if (task == null) return false;
        synchronized (mTasks) {
            mTasks.add(task);
        }
        return true;
    }

    private boolean addTask(JsonNode task) {
        String exchange = task.get(KEY_EXCHANGE).asText();
        JsonNode data = task.get(KEY_DATA);

        if (task.hasNonNull(KEY_SCHEDULE)) {
            Schedule schedule = new Schedule();
            try {
                if (!schedule.assignNode(task.get(KEY_SCHEDULE))) {
                    log.warn("Schedule JSON parse error.");
                    return false;
                }
                return addTask(schedule, exchange, data);
            } catch (Exception e) {
                log.warn("Error Scheduler Task parsing from JSON", e);
            }
        } else {
            return addTemporaryTask(task);
        }
        return false;
    }

    public final void updateTasks(JsonNode arr, String from) {
        synchronized (mTasks) {
            clear();
            if (arr == null) return;

            for (JsonNode obj : arr) {
                if (obj.isObject()) {
                    addTask(obj);
                }
            }
            initialize();
            log.info("Loading {} tasks from {}.", mTasks.size(), from);
        }
    }

    public final void executeTask(JsonNode data) {
        if (addTask(data)) {
            initialize();
        }
    }
}

