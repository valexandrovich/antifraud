package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.CustomLog;
import lombok.Getter;

import java.util.*;

@CustomLog
@Getter
public class OtpExchange {
    public static final String SCHEDULER = "otp-etl.scheduler";
    public static final String SCHEDULER_INIT = "otp-etl.scheduler.init";
    public static final String SCHEDULER_TEST = "otp-etl.scheduler.test";
    public static final String DOWNLOADER = "otp-etl.downloader";
    public static final String IMPORTER = "otp-etl.importer";
    public static final String ENRICHER = "otp-etl.enricher";
    public static final String PRIORITY_ENRICHER = "otp-etl.priority-enricher";
    public static final String STATUS_LOGGER = "otp-etl.statuslogger";
    public static final String NOTIFICATION = "otp-etl.notification";
    public static final String DWH = "otp-etl.dwh";
    public static final String REPORT = "otp-etl.report";

    private static final Map<String, Map<String, Object>> queueParams = new HashMap<>();

    private static final String QUEUE_PARAMS_JSON = ("{" +

            "'otp-etl.enricher': {'x-max-priority': 10}" +

            "}")
            .replace('\'', '"');

    private static class JsonObjectIterable implements Iterable<Map.Entry<String, JsonNode>> {
        private final ObjectNode node;
        public JsonObjectIterable(ObjectNode node) {
            this.node = node;
        }

        @Override
        public Iterator<Map.Entry<String, JsonNode>> iterator() {
            return node.fields();
        }
    }

    private OtpExchange() {
        // Nothing
    }

    private static Object getObjectFromNode(JsonNode node) {
        if (node == null) return null;
        switch (node.getNodeType()) {
            case STRING:
                return node.asText();
            case NUMBER:
                if (node.isIntegralNumber()) {
                    return node.asInt();
                } else {
                    return node.asDouble();
                }
            case BOOLEAN:
                return node.asBoolean();
            default:
                return null;
        }
    }

    private static Map<String, Object> getValues(JsonNode node) {
        if (node == null || !node.isObject()) return Collections.emptyMap();
        Map<String, Object> res = new HashMap<>();
        ObjectNode values = (ObjectNode) node;
        JsonObjectIterable queueList = new JsonObjectIterable(values);
        for (var entry : queueList) {
            Object v = getObjectFromNode(entry.getValue());
            if (v != null) {
                res.put(entry.getKey(), v);
            }
        }
        return res;
    }

    public static Map<String, Object> getQueueParams(String queue) {
        return queueParams.getOrDefault(queue, null);
    }

    public static void prepareSpecialQueues() {
        List<String> queues = new ArrayList<>();
        for (var entry : queueParams.entrySet()) {
            String queue = entry.getKey();
            Map<String, Object> params = entry.getValue();
            if (params != null && !params.isEmpty()) {
                if (!Utils.prepareRabbitMQQueue(queue)) {
                    log.warn("Queue {} can't be prepared.", queue);
                } else {
                    queues.add(queue);
                }
            }
        }
        if (queues.isEmpty()) {
            log.info("-- no special queues prepared.");
        } else {
            log.info("-- special queues prepared: {}", String.join(", ", queues));
        }
    }

    static {
        JsonNode node = Utils.getJsonNode(QUEUE_PARAMS_JSON);
        if (node != null && node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            JsonObjectIterable fields = new JsonObjectIterable(objectNode);
            for (var entry : fields) {
                Map<String, Object> values = getValues(entry.getValue());
                if (values.isEmpty()) {
                    queueParams.remove(entry.getKey());
                } else {
                    queueParams.put(entry.getKey(), values);
                }
            }
        }
    }
}
