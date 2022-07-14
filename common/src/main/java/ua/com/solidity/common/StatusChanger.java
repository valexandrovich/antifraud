package ua.com.solidity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.helpers.MessageFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatusChanger {
    public static final String UNIT_BYTES = "bytes";
    private static final String PERIOD_PROPERTY = "statusChanger.defaultPeriodMSecs";
    private static final long DEFAULT_PERIOD_MS = 1000;
    private static final Map<TimerTask, List<StatusChanger>> periodMap = new HashMap<>();
    private static boolean initialized = false;
    private static long defaultPeriod;

    @Getter
    @Setter
    @NoArgsConstructor
    private static class StatusLogRecord {
        public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        private UUID id;
        private String name;
        private String userName;
        private String started;
        private String finished;
        private String status;
        private Long progress;
        private String unit;

        @JsonIgnore
        public final void setStartedDateTime(LocalDateTime datetime) {
            started = format.format(datetime);
        }

        @JsonIgnore
        public final void setFinishedDateTime(LocalDateTime datetime) {
            finished = format.format(datetime);
        }
    }

    private TimerTask task;
    private final String name;
    private Long processedVolume;
    private Long volume;
    private Long currentPercent;
    private boolean changed = false;
    private boolean completed = false;
    private final StatusLogRecord statusObject = new StatusLogRecord();

    private static void initializationNeeded() {
        if (initialized) return;
        try {
            defaultPeriod = Integer.parseInt(Utils.getContextProperty(PERIOD_PROPERTY, String.valueOf(DEFAULT_PERIOD_MS)));
        } catch(Exception e) {
            defaultPeriod = DEFAULT_PERIOD_MS;
        }
        initialized = true;
    }

    private static synchronized void remove(StatusChanger changer) {
        List<StatusChanger> items = periodMap.get(changer.task);
        items.remove(changer);
        if (items.isEmpty()) {
            changer.task.cancel();
            periodMap.remove(changer.task);
        }
    }

    private static synchronized void forceExecute(StatusChanger changer) {
        if (changer.changed) {
            Utils.sendRabbitMQMessage(OtpExchange.STATUS_LOGGER, Utils.objectToJsonString(changer.statusObject));
            changer.changed = false;
            if (changer.completed) {
                remove(changer);
            }
        }
    }

    private static synchronized void execute(TimerTask task) {
        List<StatusChanger> items = periodMap.get(task);
        for (int i = items.size() - 1; i >= 0; --i) {
            forceExecute(items.get(i));
        }
    }

    private static synchronized TimerTask add(long period, StatusChanger item) {
        period = period <= 0 ? defaultPeriod : period;
        TimerTask task = null;
        List<StatusChanger> list = null;

        for (var info : periodMap.entrySet()) {
            TimerTask key = info.getKey();
            if (Utils.getPeriodicExecutionTaskPeriod(key) == period) {
                task = key;
                list = info.getValue();
                break;
            }
        }

        if (task == null) {
            list = new ArrayList<>();
            list.add(item);
            task = Utils.periodicExecute(period, StatusChanger::execute);
            periodMap.put(task, list);
        } else {
            list.add(item);
        }
        return task;
    }

    @SuppressWarnings("unused")
    public StatusChanger(UUID id, String name, String userName, long period) {
        initializationNeeded();
        if (id == null) {
            id = UUID.randomUUID();
        }
        this.name = name;
        statusObject.id = id;
        statusObject.userName = userName;
        statusObject.setStartedDateTime(LocalDateTime.now());
        task = add(period,this);
    }

    @SuppressWarnings("unused")
    public StatusChanger(String name, String userName, long period) {
        this(null, name, userName, period);
    }

    @SuppressWarnings("unused")
    public StatusChanger(UUID id, String name, String userName) {
        this(id, name, userName, defaultPeriod);
    }

    @SuppressWarnings("unused")
    public StatusChanger(String name, String userName) {
        this(null, name, userName, defaultPeriod);
    }

    public synchronized Long getProcessedVolume() {
        return processedVolume;
    }

    public synchronized void setStatus(String value) {
        if (!StringUtils.equals(statusObject.status, value)) {
            statusObject.status = value;
            changed = true;
        }
    }

    public final void update() {
        if (!completed) {
            forceExecute(this);
        }
    }

    public synchronized void newStage(String stage, String status, long volume, String customUnit, long period) {
        this.volume = volume;
        this.processedVolume = 0L;
        this.currentPercent = 0L;
        customUnit = customUnit == null || customUnit.isBlank() ? "rows" : customUnit;
        statusObject.name = stage != null && !stage.isBlank() ? name + ":" + stage : name;
        statusObject.status = status;
        statusObject.progress = 0L;
        statusObject.unit = volume > 0 ? "%" : customUnit;
        if (task != null && period > 0 && Utils.getPeriodicExecutionTaskPeriod(task) != period) {
            remove(this);
            task = add(period, this);
        }
        changed = true;
        update();
    }

    @SuppressWarnings("unused")
    public final void newStage(String stage, String status, long volume, String customUnit) {
        newStage(stage, status, volume, customUnit, Utils.getPeriodicExecutionTaskPeriod(task));
    }

    @SuppressWarnings("unused")
    public final void newStage(String stage, String status, long volume) {
        newStage(stage, status, volume, null, Utils.getPeriodicExecutionTaskPeriod(task));
    }

    @SuppressWarnings("unused")
    public final void newStage(String stage, String status, String customUnit) {
        newStage(stage, status, 0, customUnit, Utils.getPeriodicExecutionTaskPeriod(task));
    }

    @SuppressWarnings("unused")
    public final void newStage(String stage, String status) {
        newStage(stage, status, 0, null, Utils.getPeriodicExecutionTaskPeriod(task));
    }

    private void doOnComplete() {
        statusObject.progress = 100L;
        statusObject.unit = "%";
    }

    public final void stageComplete(String status) {
        synchronized (this) {
            doOnComplete();
            statusObject.status = status;
            changed = true;
            update();
        }
    }

    @SuppressWarnings("unused")
    public final void stageComplete(String statusPattern, Object ...args) {
        stageComplete(MessageFormatter.arrayFormat(statusPattern, args).getMessage());
    }

    public final synchronized void setProcessedVolume(long volume) {
        if (volume == processedVolume) return;
        processedVolume = volume;
        if (this.volume > 0) {
            if (processedVolume > this.volume) {
                processedVolume = this.volume;
            }
            long percent = processedVolume * 100 / this.volume;
            if (percent != currentPercent) {
                currentPercent = percent;
                statusObject.progress = currentPercent;
                changed = true;
            }
        } else {
            statusObject.progress = processedVolume;
            changed = true;
        }
    }

    public final void addProcessedVolume(long volume) {
        if (volume == 0) return;
        setProcessedVolume(processedVolume + volume);
    }

    private synchronized void finalStatus(String status) {
        statusObject.setFinishedDateTime(LocalDateTime.now());
        statusObject.status = status;
        completed = true;
        changed = true;
        update();
    }

    public final void error(String status) {
        finalStatus(status);
    }

    public final void complete(String status) {
        synchronized(this) {
            statusObject.name = name;
            doOnComplete();
            finalStatus(status);
        }
    }
}
