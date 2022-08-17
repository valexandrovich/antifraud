package ua.com.solidity.common.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.CustomLog;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import ua.com.solidity.common.DBUtils;
import ua.com.solidity.common.StatusChanger;
import ua.com.solidity.common.Utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.*;

@CustomLog
@SuppressWarnings("unused")
public class ServiceMonitor {
    public static final long MIN_UPDATE_DELAY = 250;  // 0.25s
    public static final long MIN_CLEAN_DELAY = 10000; // 10s

    public static final long DEFAULT_SCAN_DELAY = 500; // 0.5s
    public static final long DEFAULT_UPDATE_DELAY = 1000; // 1s
    public static final long DEFAULT_CLEAN_DELAY = 120000; // 2m

    public static long SCAN_DELAY = DEFAULT_SCAN_DELAY;
    public static long UPDATE_DELAY = DEFAULT_UPDATE_DELAY;
    public static long CLEAN_DELAY = DEFAULT_CLEAN_DELAY;

    public static String SCAN_DELAY_PROP = "otp.service-monitor.scan";
    public static String UPDATE_DELAY_PROP = "otp.service-monitor.update";
    public static String CLEAN_DELAY_PROP = "otp.service-monitor.clean";

    public static String SERVICE_NAME = "otp.service.name";
    public static String SERVICE_VERSION = "otp.service.version";

    public static String UNDEFINED_SERVICE_NAME = "(undefined)";
    public static String UNDEFINED_SERVICE_VERSION = "(undefined)";

    static ServiceMonitor instance = null;
    private static final UUID service_id = UUID.randomUUID();
    static final Timer timer = new Timer("service-monitor");
    private static final TimerTask flushTask = new ServiceFlushTask();
    private static final TimerTask scanTask = new ServiceScanTask();
    private static final List<StatusChanger> jobs = new ArrayList<>();

    static Connection connection;

    private String processStatus = null;
    private final Metric memoryUsed;
    private final Metric memoryCommitted;
    private Metric processorStats = null;
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    private final String serviceName;
    static final ObjectMapper mapper = new JsonMapper();
    private final int processorCount = Runtime.getRuntime().availableProcessors();
    private long cpuTimeCollected;
    private long cpuTimeCollectedAt;
    private PreparedStatement updateStatement;
    private PreparedStatement lookupStatement;

    private static class ServiceScanTask extends TimerTask {
        @Override
        public void run() {
            if (instance == null) return;
            instance.collectMemory();
            instance.collectCPU();
        }
    }

    private static class ServiceFlushTask extends TimerTask {
        @Override
        public void run() {
            if (instance == null) return;
            ObjectNode node = Metric.flush();
            ArrayNode jobs = jobsFlush();
            instance.flush(node, jobs);
        }
    }

    public static void addJob(StatusChanger changer) { // don't use directly
        synchronized(jobs) {
            jobs.add(changer);
        }
    }

    public static void removeJob(StatusChanger changer) { // don't use directly
        synchronized(jobs) {
            jobs.remove(changer);
        }
    }

    public static boolean initialize(ApplicationContext context) {
        if (instance == null) {
            if (context == null) {
                if (!Utils.checkApplicationContext()) {
                    log.error("==> ServiceMonitor start failed.");
                    return false;
                }
            } else {
                Utils.setApplicationContext(context);
            }

            UPDATE_DELAY = Math.max(MIN_UPDATE_DELAY, Utils.getLongContextProperty(UPDATE_DELAY_PROP, DEFAULT_UPDATE_DELAY));
            CLEAN_DELAY = Math.max(MIN_CLEAN_DELAY, Utils.getLongContextProperty(CLEAN_DELAY_PROP, DEFAULT_CLEAN_DELAY));
            SCAN_DELAY = Math.min(UPDATE_DELAY, Utils.getLongContextProperty(SCAN_DELAY_PROP, DEFAULT_SCAN_DELAY));

            instance = new ServiceMonitor(Utils.getApplicationContext());
        }

        return true;
    }

    public static boolean initialize() {
        return initialize(null);
    }

    public static boolean initializeCleaner() {
        return ServiceMonitorCleaner.initialize();
    }

    public static boolean initializeStateLookup() {
        return ServiceMonitorState.initialize();
    }

    public static boolean initialize(ApplicationContext context, boolean service, boolean cleaner, boolean stateLookup) {
        if (context != null) {
            Utils.setApplicationContext(context);
        }
        return initialize(service, cleaner, stateLookup);
    }

    public static boolean initialize(boolean service, boolean cleaner, boolean stateLookup) {
        return (service && initialize()) || (cleaner && initializeCleaner()) || (stateLookup && initializeStateLookup());
    }

    public static Metric getStatistic(String name) {
        return Metric.create(name);
    }

    private ServiceMonitor(ApplicationContext context) {
        Environment env = context.getEnvironment();
        String name = env.getProperty(SERVICE_NAME, UNDEFINED_SERVICE_NAME);
        String version = env.getProperty(SERVICE_VERSION, UNDEFINED_SERVICE_VERSION);
        serviceName = name + "/" + version;

        log.info("==> ServiceMonitor started (name: {}, id:{}).", serviceName, service_id);

        collectCPUTime();
        memoryUsed = getStatistic("memory/used");
        memoryCommitted = getStatistic("memory/committed");
        if (threads.isThreadCpuTimeSupported()) {
            processorStats = getStatistic("CPU");
        }
        schedule(flushTask, 0);  // for flush data
        schedule(scanTask, 0);    // for scan memory and cpu
    }

    public static void schedule(TimerTask task, long delayAdd) {
        long delay = Instant.now().toEpochMilli() % 1000;
        timer.schedule(task, (delay > 0 ? 1000 - delay: 0) + delayAdd, UPDATE_DELAY);
    }

    private void collectMemory() {
        memoryUsed.putValue((double)memory.getHeapMemoryUsage().getUsed() / 1048576); // in Mb
        memoryCommitted.putValue((double)memory.getHeapMemoryUsage().getCommitted() / 1048576); // in Mb
    }

    private void collectCPUTime() {
        long cpuTime = 0;
        cpuTimeCollectedAt = Instant.now().toEpochMilli();
        for (long threadId : threads.getAllThreadIds()) {
            cpuTime += threads.getThreadCpuTime(threadId);
        }
        cpuTimeCollected = cpuTime;
    }

    private synchronized void collectCPU() {
        if (processorStats == null) return;
        long oldCollected = cpuTimeCollected;
        long oldCollectedAt = cpuTimeCollectedAt;
        collectCPUTime();
        long deltaMs = cpuTimeCollectedAt - oldCollectedAt;
        double deltaCPU = cpuTimeCollected - oldCollected;
        processorStats.putValue(deltaMs <= 0 ? 0 : deltaCPU / instance.processorCount / 1e6);
    }

    public static synchronized void setProcessStatus(String value) {
        if (instance == null) return;
        instance.processStatus = value == null || value.isBlank() ? null : value.trim();
    }

    private static ArrayNode jobsFlush() {
        synchronized(jobs) {
            ArrayNode res = JsonNodeFactory.instance.arrayNode();
            for (var item: jobs) {
                res.add(item.getJsonObject());
            }
            return res.isEmpty() ? null : res;
        }
    }

    private synchronized void flush(ObjectNode stats, ArrayNode jobs) {
        ObjectNode res = JsonNodeFactory.instance.objectNode();
        String state = "idle";
        if (stats == null && jobs == null && processStatus != null) {
            state = processStatus;
        }
        res.put("state", state);
        if (stats != null) res.set("metrics", stats);
        if (jobs != null) res.set("jobs", jobs);
        saveData(res);
    }

    static boolean connectionNeeded() {
        synchronized(ServiceMonitor.class) {
            if (connection != null) return true;
            try {
                connection = DBUtils.createConnection("spring.datasource", "reWriteBatchedInserts=true");
                return true;
            } catch (Exception e) {
                log.error("DBConnection not established.", e);
            }
            return false;
        }
    }

    private boolean updateStatementNeeded()  {
        synchronized (ServiceMonitor.class) {
            if (updateStatement != null) return true;
            if (connectionNeeded()) {
                try {
                    updateStatement = connection.prepareStatement(
                            "select 0 from service_monitor_update(?::uuid, ?, ?::jsonb)");
                } catch(Exception e) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }

    private void saveData(ObjectNode node) {
        if (updateStatementNeeded()) {
            try {
                updateStatement.setObject(1, service_id);
                updateStatement.setString(2, serviceName);
                updateStatement.setObject(3, node == null ? null : node.toString());
                updateStatement.execute();
                log.info("$monitor$flush: {}", node == null ? "(null)" : node.toString());
            } catch (Exception e) {
                log.error("Service-monitor update error.", e);
            }
        }
    }
}
