package ua.com.solidity.common.monitoring;

import lombok.CustomLog;

import java.sql.PreparedStatement;
import java.util.TimerTask;

@CustomLog
@SuppressWarnings("unused")
public class ServiceMonitorCleaner {
    private static ServiceMonitorCleaner instance = null;
    private PreparedStatement cleanStatement;

    private static class ServiceMonitorCleanTask extends TimerTask {
        @Override
        public void run() {
            if (instance != null) instance.cleanMonitor();
        }
    }

    public static boolean initialize() {
        if (instance == null) {
            instance = new ServiceMonitorCleaner();
        }
        return true;
    }

    private ServiceMonitorCleaner() {
        TimerTask cleanTask = new ServiceMonitorCleanTask();
        ServiceMonitor.TIMER.schedule(cleanTask, 0, ServiceMonitor.getCleanDelay());
        log.info("==> ServiceMonitor cleaner started vs delay = {}.", ServiceMonitor.getCleanDelay());
    }

    private boolean cleanStatementNeeded() {
        synchronized (ServiceMonitor.class) {
            if (cleanStatement != null) return true;
            if (ServiceMonitor.connectionNeeded()) {
                try {
                    cleanStatement = ServiceMonitor.connection.prepareStatement("select 0 from service_monitor_clean()");
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        }
    }

    protected final void cleanMonitor() {
        synchronized (ServiceMonitor.class) {
            if (cleanStatementNeeded()) {
                try {
                    cleanStatement.execute();
                    log.info("$monitor-clean$===============> Cleaning...");
                } catch (Exception e) {
                    log.error("ServiceMonitor clean error.", e);
                }
            }
        }
    }
}
