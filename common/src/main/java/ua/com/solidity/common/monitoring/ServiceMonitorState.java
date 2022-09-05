package ua.com.solidity.common.monitoring;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import ua.com.solidity.common.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.TimerTask;

@CustomLog
@SuppressWarnings("unused")
public class ServiceMonitorState {
    private static boolean initialized = false;
    private static final TimerTask lookupTask = new ServiceMonitorLookupStateTask();
    private static PreparedStatement lookupStatement;
    private static String currentStateString = null;

    private static class ServiceMonitorLookupStateTask extends TimerTask {
        @Override
        public void run() {
            if (lookupStatementNeeded()) {
                try {
                    synchronized(ServiceMonitor.class) {
                        ResultSet res = lookupStatement.executeQuery();
                        Object obj = res.getObject(1);
                        res.close();
                        if (obj instanceof String) {
                            currentStateString = (String) obj;
                        } else {
                            currentStateString = null;
                        }
                    }
                    log.info("$monitor-state$Data:{}", currentStateString);
                } catch (Exception e) {
                    log.error("ServiceMonitor lookup state error.", e);
                }
            }
        }
    }

    private ServiceMonitorState() {
        // nothing
    }

    public static boolean initialize() {
        initialized = true;
        ServiceMonitor.schedule(lookupTask, ServiceMonitor.getUpdateDelay());
        log.info("==> ServiceMonitor state getter started.");
        return true;
    }

    private static boolean lookupStatementNeeded() {
        if (!initialized) return false;
        if (lookupStatement != null) return true;
        if (ServiceMonitor.connectionNeeded()) {
            try {
                lookupStatement = ServiceMonitor.connection.prepareStatement("select * from service_monitor_state()");
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static String getStateString() {
        synchronized(ServiceMonitor.class) {
            return currentStateString;
        }
    }

    public static JsonNode getState() {
        synchronized(ServiceMonitor.class) {
            return Utils.getJsonNode(currentStateString);
        }
    }
}
