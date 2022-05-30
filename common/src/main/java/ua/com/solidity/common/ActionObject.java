package ua.com.solidity.common;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)

public abstract class ActionObject {
    private static final String KEY_ACTION = "action";
    private static final Map<String, Class<? extends ActionObject>> registeredActions = new HashMap<>();

    protected String action;

    public static ActionObject getAction(JsonNode node) {
        Class<? extends ActionObject> clazz;
        if (node == null || !node.isObject() || !node.hasNonNull(KEY_ACTION) ||
                (clazz = registeredActions.getOrDefault(node.get(KEY_ACTION).asText(""),
                        null)) == null) return null;
        return Utils.jsonToValue(node, clazz);
    }

    @SuppressWarnings("unused")
    public static ActionObject getAction(String json) {
        return json == null || json.isBlank() ? null : getAction(Utils.getJsonNode(json));
    }

    public static void register(Class<? extends ActionObject> clazz, String ...actions) {
        for (var action : actions) {
            if (action != null && !action.isBlank() && clazz != null) {
                registeredActions.put(action, clazz);
            }
        }
    }

    protected abstract boolean doValidate();
    protected abstract boolean doExecute();

    public final boolean execute() {
        return doValidate() && doExecute();
    }
}
