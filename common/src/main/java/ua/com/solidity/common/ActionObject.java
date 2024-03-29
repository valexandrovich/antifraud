package ua.com.solidity.common;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@CustomLog
public abstract class ActionObject {
    private static final String KEY_ACTION = "action";
    private static final Map<String, Class<? extends ActionObject>> REGISTERED_ACTIONS = new HashMap<>();
    protected String action;
    protected JsonNode node;
    private RabbitMQActionTask associatedTask;

    public static ActionObject getAction(JsonNode node) {
        Class<? extends ActionObject> clazz;
        if (node == null || node.isNull() || !node.isObject() || !node.hasNonNull(KEY_ACTION) ||
                (clazz = REGISTERED_ACTIONS.getOrDefault(node.get(KEY_ACTION).asText(""),
                        null)) == null) return null;
        ActionObject res = Utils.jsonToValue(node, clazz);
        res.node = node;
        return res;
    }

    void setAssociatedTask(RabbitMQActionTask task) {
        associatedTask = task;
    }

    @SuppressWarnings("unused")
    public static ActionObject getAction(String json) {
        return json == null || json.isBlank() ? null : getAction(Utils.getJsonNode(json));
    }

    public static void register(Class<? extends ActionObject> clazz, String ...actions) {
        for (var action : actions) {
            if (action != null && !action.isBlank() && clazz != null) {
                REGISTERED_ACTIONS.put(action, clazz);
            }
        }
    }

    protected abstract boolean doValidate();
    protected abstract boolean doExecute();

    protected final void acknowledge() {
        if (associatedTask != null) {
            associatedTask.acknowledge(true);
        }
    }

    public final boolean execute() {
        try {
            return doValidate() && doExecute();
        } catch (Exception e) {
            log.error("Can't handle action {}.", node, e);
        }
        return false;
    }
}
