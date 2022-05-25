package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.NonNull;
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


@CustomLog
public class Scheduler {
    public static final String KEY_EXCHANGE = "exchange";
    public static final String KEY_SCHEDULE = "schedule";
    public static final String KEY_DATA = "data";
    public static final String KEY_SLEEP_MS = "sleep_ms";

    public static final String MSG_INIT = "init";

    public static final String LOG_HANDLED = "{} <= {}";
    public static final String LOG_HANDLED_IMMEDIATE = "* {} <= {}";
    public static final String LOG_HANDLED_TEMP = "- ({}) {} <= {}";

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
        private LocalDateTime datetime;

        private Task(Schedule schedule, String exchange, JsonNode data) {
            this.schedule = schedule;
            this.exchange = exchange;
            this.data = data;
            this.datetime = schedule.nearest(now(), false);
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

        public static Task createTemporaryTask(String exchange, JsonNode data, long millis) {
            return new Task(millis, exchange, data);
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
        LocalDateTime datetime;

        public SendThread(Scheduler aScheduler) {
            super("scheduler");
            scheduler = aScheduler;
        }

        private boolean exitFromNextTask() {
            long milliseconds = datetime.toInstant(ZoneOffset.UTC).toEpochMilli() - now().toInstant(ZoneOffset.UTC).toEpochMilli();
            if (milliseconds >= 0) {
                ++milliseconds; // protection for nanoseconds difference near 1 ms
            }
            if (milliseconds > 0) {
                try {
                    sleep(milliseconds);
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
                datetime = scheduler.sendMessages();
                if (datetime == null || exitFromNextTask()) break;
            }
        }
    }

    private final List<Task> mTasks = new ArrayList<>();
    private SendThread mSender;

    public final void clear() {
        stopSenderThread();
        synchronized (mTasks) {
            mTasks.clear();
        }
    }

    private void doSendMessage(String exchange, String message, boolean isTemporary, boolean isImmediate) {
        Utils.sendRabbitMQMessage(exchange, message);
        if (isTemporary) {
            log.info(LOG_HANDLED_TEMP, mTasks.size() - 1, exchange, message);
        } else if (isImmediate) {
            log.info(LOG_HANDLED_IMMEDIATE, exchange, message);
        } else {
            log.info(LOG_HANDLED, exchange, message);
        }
    }

    private void sendMessage(Task task) {
        doSendMessage(task.exchange, task.data.toString(), task.isTemporary(), false);
    }

    private void refreshBySendMessage() {
        clear();
        Utils.sendRabbitMQMessage(config.getInitName(), MSG_INIT);
    }

    private void refreshByLoadingInitFile() {
        updateTasks(Utils.getJsonNode(schedulerInitTasks), "initTasks");
    }

    private void logTasks(boolean someTasksExecuted) {
        synchronized(mTasks) {
            if (mTasks.isEmpty()) {
                if (someTasksExecuted) {
                    log.info("-- All tasks completed. --");
                } else {
                    log.info("-- No active tasks found. --");
                }
            } else {
                log.info("-- {} tasks loaded. --", mTasks.size());
            }
        }
    }

    private void refreshByDatabaseRequest() {
        List<SchedulerEntity> list = SchedulerEntity.getAll();
        clear();
        boolean someTasksExecuted = false;
        if (list != null) {
            for (SchedulerEntity entity : list) {
                if (entity.getSchedule() == null) {
                    doSendMessage(entity.getExchange(), entity.getData().toString(), false, true);
                    someTasksExecuted = true;
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
        invalidate();
        logTasks(someTasksExecuted);
    }

    public final void init() {
        if (config.getInit().equals("init")) {
            refreshBySendMessage();
        } else if (config.getInit().equals("debug")) {
            refreshByLoadingInitFile();
        } else {
            refreshByDatabaseRequest();
        }
    }

    public final void refresh() {
        refreshByDatabaseRequest();
    }

    protected final LocalDateTime sendMessages(/*LocalDateTime now*/) {
        synchronized (mTasks) {
            int count;
            do {
                count = 0;
                LocalDateTime now = now();
                for (Task task : mTasks) {
                    if ((task.datetime.isBefore(now) || task.datetime.isEqual(now))) {
                        sendMessage(task);
                        task.next(now);
                        ++count;
                    } else break;
                }
                if (count > 0) {
                    mTasks.removeIf(Task::removeNeeded);
                    Collections.sort(mTasks);
                }
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

    public final void invalidate() {
        stopSenderThread();
        synchronized(mTasks) {
            Collections.sort(mTasks);
        }
        startSenderThread();
    }

    private boolean addTemporaryTask(long millis, String exchange, JsonNode data) {
        if (exchange == null) return false;
        if (millis <= 0) {
            doSendMessage(exchange, data != null ? data.toString() : "", false, true);
            return false;
        } else {
            synchronized (mTasks) {
                mTasks.add(Task.createTemporaryTask(exchange, data, millis));
            }
        }
        return true;
    }

    private boolean addTask(Schedule schedule, String exchange, JsonNode data) {
        synchronized (mTasks) {
            Task task = Task.createTask(schedule, exchange, data);
            if (task == null) return false;
            mTasks.add(task);
            return true;
        }
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
            return addTemporaryTask(
                    task.hasNonNull(KEY_SLEEP_MS) ? task.get(KEY_SLEEP_MS).asLong(0) : 0,
                    exchange, data);
        }
        return false;
    }

    public final void updateTasks(JsonNode arr, String from) {
        clear();
        if (arr == null || !arr.isArray()) {
            log.error("UpdateTasks error. Invalid Argument, json-array needed.");
            return;
        }

        for (JsonNode obj : arr) {
            if (obj.isObject()) {
                addTask(obj);
            }
        }
        invalidate();
        log.info("{} tasks loaded from {}.", mTasks.size(), from);
    }

    private void logAddedTask(JsonNode data) {
        synchronized(mTasks) {
            if (data.hasNonNull(KEY_SCHEDULE)) {
                log.info("+ ({}) task: {} <= {}, schedule: {} --", mTasks.size(), data.get(KEY_EXCHANGE).asText(), data.get(KEY_DATA).toString(), data.get(KEY_SCHEDULE).asText());
            } else {
                log.info("+ ({}, ms:{}) task: {} <= {}", mTasks.size(), data.get(KEY_SLEEP_MS).asLong(0), data.get(KEY_EXCHANGE).asText(), data.get(KEY_DATA).toString());
            }
        }
    }

    public final void executeTask(JsonNode data) {
        if (addTask(data)) {
            logAddedTask(data);
            invalidate();
        }
    }
}

