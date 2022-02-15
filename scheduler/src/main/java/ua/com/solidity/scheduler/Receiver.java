package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.RabbitMQReceiver;
import ua.com.solidity.db.entities.SchedulerEntity;

@Slf4j
@Component
public class Receiver extends RabbitMQReceiver {
    public static final String KEY_ACTION = "action";
    public static final String KEY_DATA = "data";
    public static final String KEY_GROUP = "group";
    public static final String VALUE_UNDEFINED = "(undefined)";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_EXEC = "exec";
    public static final String ACTION_PING = "ping";
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
                scheduler.refresh();
                return;
            }
        }
        log.error("SWITCH ERROR: command must have \"group\" argument for switch to.");
    }

    @Override
    protected Object handleMessage(String queue, String message) {
        try {
            JsonNode node = new ObjectMapper().readTree(message);

            if (node == null || node.isEmpty()) {
                log.warn("Empty command received.");
            } else {
                String action = node.get(KEY_ACTION).asText();
                if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case ACTION_UPDATE:
                            scheduler.updateTasks(node.get(KEY_DATA), "RABBIT");
                            break;

                        case ACTION_EXEC: {
                            scheduler.executeTask(node);
                            break;
                        }

                        case ACTION_PING:
                            log.info("-- PING: {} --", node.hasNonNull(KEY_DATA) ? node.get(KEY_DATA).asText(VALUE_UNDEFINED) : VALUE_UNDEFINED);
                            break;

                        case ACTION_SWITCH:
                            handleSwitch(node);
                            break;

                        case ACTION_REFRESH:
                            log.info("-- REFRESH received. --");
                            scheduler.refresh();
                            break;

                        case ACTION_CLEAR:
                            log.info("--CLEAR received (use REFRESH for restore tasks) --");
                            scheduler.clear();
                            break;

                        default:
                            log.warn("Illegal action \"{}\"", action);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Receive message error.", e);
        }
        return true;
    }
}
