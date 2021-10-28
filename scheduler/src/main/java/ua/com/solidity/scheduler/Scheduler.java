package ua.com.solidity.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
public class Scheduler {
    public static final String KEY_TOPIC = "topic";
    public static final String KEY_ROUTE = "routingKey";
    public static final String KEY_SCHEDULE = "schedule";
    public static final String KEY_DATA = "data";

    public static final String MSG_REFRESH = "init";

    public static final String LOG_HANDLED = "handled: task for topic={} routingKey={} : {}";

    @Autowired
    private Config config;

    private static class Task implements Comparable<Task> {
        public static final int BEFORE = -1;
        public static final int AFTER = 1;
        public static final int EQUAL = 0;

        public final Schedule schedule;
        public final String topic;
        public final String routingKey;
        public final String data;
        private LocalDateTime datetime = null;
        private boolean mIsActive = true;

        private Task(Schedule schedule, String topic, String routingKey, String data) {
            this.schedule = schedule;
            this.topic = topic;
            this.routingKey = routingKey;
            this.data = data;
        }

        public static Task createTask(Schedule schedule, String topic, String routingKey, String data) {
            if (schedule == null) return null;
            return new Task(schedule, topic, routingKey, data);
        }

        public final boolean isActive() { return mIsActive && datetime != null; }

        public final boolean getActive() { return mIsActive; }
        public final void setActive(boolean value) { mIsActive = value; }

        @Override
        public int hashCode() {
            return Objects.hash(topic, routingKey, schedule, data);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Task)) return false;
            return compareTo((Task) o) == EQUAL;
        }

        @Override
        public int compareTo(@NotNull Scheduler.Task o) {
            if (o.datetime == null || !o.mIsActive) {
                return BEFORE;
            } else if (datetime == null || mIsActive) {
                return AFTER;
            } else {
                int compareResult = datetime.compareTo(o.datetime);
                if (compareResult == EQUAL) {
                    compareResult = o.topic.compareTo(topic);
                    if (compareResult == EQUAL) {
                        compareResult = o.routingKey.compareTo(routingKey);
                    }
                }

                return compareResult;
            }
        }

        public final LocalDateTime getDatetime() {
            return datetime;
        }

        public final void next(LocalDateTime aDatetime) {
            datetime = schedule.nearest(aDatetime, true);
        }
    }

    private static class SendThread extends Thread {
        public static final long IDLE_DELAY_MSECS = 60 * 60 * 1000L;  // 30 minutes
        public final Scheduler scheduler;

        public SendThread(Scheduler aScheduler) {
            scheduler = aScheduler;
        }

        @Override
        public void run() {
            while (!interrupted()) {
                LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
                LocalDateTime datetime = scheduler.sendMessages(now);

                if (datetime == null) {
                    try {
                        sleep(IDLE_DELAY_MSECS);
                    } catch (InterruptedException e) {
                        interrupt();
                    }

                    if (!isInterrupted()) {
                        scheduler.refresh();
                    }
                } else {
                    long seconds = now.toEpochSecond(ZoneOffset.UTC) - datetime.toEpochSecond(ZoneOffset.UTC);
                    if (seconds > 0) {
                        try {
                            sleep(seconds * 1000);
                        } catch (InterruptedException e) {
                            interrupt();
                            return;
                        }
                    }
                }
            }
        }
    }

    private final List<Task> mTasks = new ArrayList<>();
    private final RabbitTemplate rabbitTemplate;
    private SendThread mSender;

    public Scheduler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public final void clear() {
        stopSenderThread();
        mTasks.clear();
    }

    private void sendMessage(Task task) {
        rabbitTemplate.convertAndSend(task.topic, task.routingKey, task.data);
        log.info(LOG_HANDLED, task.topic, task.routingKey, task.data);
    }

    public final void refresh() {
        synchronized(mTasks) {
            rabbitTemplate.convertAndSend(config.getTopicExchangeName(), config.getInitRoutingKey(), MSG_REFRESH);
        }
    }

    protected final LocalDateTime sendMessages(LocalDateTime now) {
        synchronized (mTasks) {
            int count;
            do {
                count = 0;

                for (int i = 0; i < mTasks.size(); ++i) {
                    Task task = mTasks.get(i);
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

    public final synchronized void initialize() {
        stopSenderThread();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        for (Task task : mTasks) {
            task.datetime = task.schedule.nearest(now, false);
        }
        Collections.sort(mTasks);
        log.info("Invalidate accepted. {} tasks loaded", mTasks.size());
        startSenderThread();
    }

    private boolean addTask(Schedule schedule, String topic, String routingKey, String data) {
        Task task = Task.createTask(schedule, topic, routingKey, data);
        if (task != null) mTasks.add(task);
        else return false;
        return true;
    }

    private boolean addTask(JSONObject task) {
        Schedule schedule = new Schedule();
        try {
            if (!schedule.putJSONObject(task.getJSONObject(KEY_SCHEDULE))) {
                // logging
                return false;
            }
            String topic = task.getString(KEY_TOPIC);
            String route = task.getString(KEY_ROUTE);
            String data = task.getString(KEY_DATA);
            return addTask(schedule, topic, route, data);
        } catch (Exception e) {
            // logging
            return false;
        }
    }

    public final boolean updateTasks(JSONArray arr) {
        clear();
        if (arr == null) return false;

        for (Object obj : arr) {
            if (obj instanceof JSONObject) {
                addTask((JSONObject) obj);
            }
        }

        initialize();

        return true;
    }

    public final boolean updateTasks(String tasksJSONString) {
        try {
            JSONArray arr = JSON.parseArray(tasksJSONString);
            return updateTasks(arr);
        } catch (Exception e) {
            log.warn("Invalid task String:\n" + tasksJSONString, e);
            return false;
        }
    }

    private Task getTask(String topic, String routingKey) {

        for (Task task : mTasks) {
            if (task.topic ==  topic && task.routingKey == routingKey) {
                return task;
            }
        }

        log.warn("Task not found (topic=\"{}\", routingKey=\"{}\")", topic, routingKey);
        return null;
    }

    public final void setTaskActive(String topic, String routingKey, boolean isActive) {
        synchronized(mTasks) {
            Task task = getTask(topic, routingKey);
            if (task == null) return;
            if (task.mIsActive != isActive) {
                task.mIsActive = isActive;
                Collections.sort(mTasks);
            }
        }
    }

    public final void setTaskActive(@NotNull JSONObject obj, boolean isActive) {
        setTaskActive(obj.getString(KEY_TOPIC), obj.getString(KEY_ROUTE), isActive);
    }

    public final void taskForceExecute(String topic, String routingKey) {
        synchronized(mTasks) {
            Task task = getTask(topic, routingKey);
            if (task == null) return;
            sendMessage(task);
        }
    }

    public final void taskForceExecute(@NotNull JSONObject obj) {
        taskForceExecute(obj.getString(KEY_TOPIC), obj.getString(KEY_ROUTE));
    }
}

