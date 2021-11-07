package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Receiver {
    public static final String KEY_ACTION = "action";
    public static final String KEY_DATA = "data";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DISABLE_TASK = "disable";
    public static final String ACTION_ENABLE_TASK = "enable";
    public static final String ACTION_EXEC_TASK = "exec";
    public static final String ACTION_REFRESH = "refresh";

    private final Scheduler scheduler;

    @Autowired
    public Receiver(Scheduler mainScheduler) {
        this.scheduler = mainScheduler;
    }

    public final void receiveMessage(String message) {
        try {
            JsonNode node = new ObjectMapper().readTree(message);

            if (node == null || node.isEmpty()) {
                log.warn("Empty command received.");
            } else {
                String action = node.get(KEY_ACTION).asText();
                if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case ACTION_UPDATE:
                            scheduler.updateTasks(node.get(KEY_DATA));
                            break;
                        case ACTION_DISABLE_TASK:
                            scheduler.setTaskActive(node.get(KEY_DATA), false);
                            break;
                        case ACTION_ENABLE_TASK:
                            scheduler.setTaskActive(node.get(KEY_DATA), true);
                            break;
                        case ACTION_EXEC_TASK:
                            scheduler.taskForceExecute(node.get(KEY_DATA));
                            break;
                        case ACTION_REFRESH:
                            scheduler.refresh();
                            break;
                        default:
                            log.warn("illegal command \"{}\"", action);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Receive message error.", e);
        }
    }
}
