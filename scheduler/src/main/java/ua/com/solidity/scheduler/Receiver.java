package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.db.entities.SchedulerEntity;

@CustomLog
@Component
public class Receiver extends RabbitMQReceiver {
    public static final String KEY_ACTION = "action";
    public static final String KEY_DATA = "data";
    public static final String KEY_ROUTE = "exchange";
    public static final String KEY_GROUP = "group";
    public static final String KEY_MESSAGE = "message";
    public static final String ACTION_INIT = "init";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_EXEC = "exec";
    public static final String ACTION_INFO = "info";
    public static final String ACTION_SEND = "send";
    public static final String ACTION_REFRESH = "refresh";
    public static final String ACTION_SWITCH = "switch";
    public static final String ACTION_CLEAR = "clear";

    private final Scheduler scheduler;

    @Autowired
    public Receiver(Scheduler mainScheduler) {
        this.scheduler = mainScheduler;
    }

    private void handleSwitch(JsonNode node) {
        if (node.hasNonNull(KEY_GROUP)) {
            String group = node.get(KEY_GROUP).asText(null);
            if (group != null) {
                SchedulerEntity.schedulerActivate(group);
                log.info("-- SWITCH({}) applied. --", group);
                scheduler.init();
                return;
            }
        }
        log.error("SWITCH ERROR: command must have \"group\" argument for switch to.");
    }

    private void doInfoAction(ObjectNode node) {
        String msg = node.hasNonNull(KEY_MESSAGE) ? node.get(KEY_MESSAGE).asText("") : "";
        if (!msg.isBlank()) {
            log.info("-- {} --", msg);
        }
    }

    private void doRedirectAction(ObjectNode node) {
        String queue = node.hasNonNull(KEY_ROUTE) ? node.get(KEY_ROUTE).asText("") : "";
        if (queue.isBlank()) {
            log.warn("Queue is undefined in REDIRECT action.");
            return;
        }
        JsonNode data = node.hasNonNull(KEY_DATA) ? node.get(KEY_DATA) : null;
        send(queue, data == null ? null : data.toPrettyString());
    }

    private void doAction(ObjectNode node) {
        String action = node.get(KEY_ACTION).asText();

        if (action != null && !action.isEmpty()) {
            switch (action.toLowerCase()) {
                case ACTION_INIT:
                    log.info("-- REFRESH received. --");
                    scheduler.init();
                    break;

                case ACTION_UPDATE:
                    scheduler.updateTasks(node.get(KEY_DATA), "rabbitMQ message");
                    break;

                case ACTION_EXEC: {
                    scheduler.executeTask(node);
                    break;
                }

                case ACTION_INFO:
                    doInfoAction(node);
                    break;

                case ACTION_SEND:
                    doRedirectAction(node);
                    break;

                case ACTION_SWITCH:
                    handleSwitch(node);
                    break;

                case ACTION_REFRESH:
                    log.info("-- REFRESH received. --");
                    scheduler.refresh();
                    break;

                case ACTION_CLEAR:
                    log.info("--CLEAR received (use REFRESH {\"action\": \"refresh\"} or SWITCH {\"action\": \"switch\", \"group\":<groupName>} for restore tasks.)--");
                    scheduler.clear();
                    break;

                default:
                    log.warn("Illegal action \"{}\"", action);
            }
        } else {
            log.warn("Action is undefined.");
        }
    }

    @Override
    protected void handleMessage(String queue, String message) {
        try {
            JsonNode node = new ObjectMapper().readTree(message);

            if (node == null || node.isEmpty()) {
                log.warn("Empty command received.");
            } else if (!node.isObject()) {
                log.warn("Command must be a json-object.");
            } else {
                doAction((ObjectNode) node);
            }
        } catch (Exception e) {
            log.error("Receive message error.", e);
        }
    }
}
