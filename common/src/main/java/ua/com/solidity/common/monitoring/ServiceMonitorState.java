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
    private static ServiceMonitorState instance = null;
    private static final TimerTask lookupTask = new ServiceMonitorLookupStateTask();
    private PreparedStatement lookupStatement;
    private String currentStateString = null;
    private JsonNode currentState = null;

    private static class ServiceMonitorLookupStateTask extends TimerTask {
        @Override
        public void run() {
            if (ServiceMonitor.instance != null && instance != null) instance.lookupState();
        }
    }

    public static boolean initialize() {
        if (instance == null) {
            instance = new ServiceMonitorState();
        }
        return true;
    }

    private ServiceMonitorState() {
        instance = this;
        ServiceMonitor.schedule(lookupTask, ServiceMonitor.UPDATE_DELAY);
        log.info("==> ServiceMonitor state getter started.");
    }

    private boolean lookupStatementNeeded() {
        if (instance == null) return false;
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

    private synchronized void lookupState() {
        if (lookupStatementNeeded()) {
             try {
                 synchronized(ServiceMonitor.class) {
                     ResultSet res = lookupStatement.executeQuery();
                     Object obj = res.getObject(0);
                     if (obj instanceof String) {
                         currentStateString = (String) obj;
                         currentState = Utils.getJsonNode(currentStateString);
                     } else {
                         currentStateString = null;
                         currentState = null;
                     }
                 }
                 log.info("$monitor$Data:{}", currentStateString);
             } catch (Exception e) {
                 log.error("ServiceMonitor lookup state error.", e);
             }
        }
    }

    public final String getStateString() {
        synchronized(ServiceMonitor.class) {
            return currentStateString;
        }
    }

    public final JsonNode getState() {
        synchronized(ServiceMonitor.class) {
            return currentState;
        }
    }
}
