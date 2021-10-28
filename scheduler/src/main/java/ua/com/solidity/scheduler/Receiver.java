package ua.com.solidity.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    private final RabbitTemplate rabbitTemplate;
    private final Scheduler scheduler;

    @Autowired
    public Receiver(RabbitTemplate rabbitTemplate, Scheduler mainScheduler) {
        this.scheduler = mainScheduler;
        this.rabbitTemplate = rabbitTemplate;
    }

    public final void receiveMessage(String message) {
        JSONObject obj;
        try {
            obj = (JSONObject) JSON.parse(message);
            if (obj == null || obj.isEmpty()) {
                log.warn("Empty command received.");
            } else {
                if (obj.containsKey(KEY_ACTION) && obj.containsKey(KEY_DATA)) {
                    switch (obj.getString(KEY_ACTION)) {
                        case ACTION_UPDATE:
                            scheduler.updateTasks(obj.getJSONArray(KEY_DATA));
                            break;
                        case ACTION_DISABLE_TASK:
                            scheduler.setTaskActive(obj.getJSONObject(KEY_DATA), false);
                            break;
                        case ACTION_ENABLE_TASK:
                            scheduler.setTaskActive(obj.getJSONObject(KEY_DATA), true);
                            break;
                        case ACTION_EXEC_TASK:
                            scheduler.taskForceExecute(obj.getJSONObject(KEY_DATA));
                            break;
                        case ACTION_REFRESH:
                            scheduler.refresh();
                            break;
                        default:
                            log.warn("illegal command\"{}\"", obj.getString(KEY_ACTION));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Receive message error.", e);
        }
    }
}
