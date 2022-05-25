package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.CustomLog;

import java.time.DayOfWeek;
import java.util.Iterator;


@CustomLog
public class DaysOfWeek implements CustomParam {

    public static final String VALUE_NONE = "NONE";
    public static final String VALUE_ALL = "ALL";

    public static final int FIRST = 1;
    public static final int SECOND = 2;
    public static final int THIRD = 4;
    public static final int FOURTH = 8;
    public static final int LAST = 16;
    public static final int LAST_BUT_ONE = 32;
    public static final int LAST_BUT_TWO = 64;
    public static final int ALL = FIRST | SECOND | THIRD | FOURTH | LAST | LAST_BUT_ONE | LAST_BUT_TWO;

    private final int[] dayOfWeekMasks = {0, 0, 0, 0, 0, 0, 0};

    public final void clear() {
        for (int i = 0; i < 7; ++i) dayOfWeekMasks[i] = 0;
    }

    @Override
    public final boolean isIgnored() {
        for (int i = 0; i < 7; ++i) if (dayOfWeekMasks[i] != 0) return false;
        return true;
    }

    protected final int getBitMaskForIndex(int index) {
        if (index > 3 || index < -3) {
            return 0;
        }
        return index >= 0 ? 1 << index : 8 << (-index);
    }

    public final boolean validateValue(int day, int dayOfWeek, int monthDayCount) {
        if (day < 0 || day >= monthDayCount || dayOfWeek < 0 || dayOfWeek > 6) return false;
        int monthFirstDayOfWeek = (dayOfWeek - day + 35) %  7; // magic solution
        int firstDayOfWeekIndex = (dayOfWeek - monthFirstDayOfWeek + 7) % 7;
        int delta = monthDayCount - firstDayOfWeekIndex;
        int count = delta / 7 + (delta % 7 == 0 ? 0 : 1);
        int index = (day - firstDayOfWeekIndex) / 7;
        return ((dayOfWeekMasks[dayOfWeek] & getBitMaskForIndex(index)) != 0) ||
                ((dayOfWeekMasks[dayOfWeek] & getBitMaskForIndex(index - count)) != 0);
    }

    private void pushValueToArrayNode(ArrayNode node, int index, int bit, int bits) {
        if ((bits & bit) != 0) node.add(index);
    }

    private JsonNode getNodeForDay(int index) {
        JsonNode node = null;
        int bits = dayOfWeekMasks[index];
        if (bits != 0) {
            if (bits == ALL) {
                node = JsonNodeFactory.instance.textNode("all");
            } else {
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                pushValueToArrayNode(arrayNode, 1, FIRST, bits);
                pushValueToArrayNode(arrayNode, 2, SECOND, bits);
                pushValueToArrayNode(arrayNode, 3, THIRD, bits);
                pushValueToArrayNode(arrayNode, 4, FOURTH, bits);
                pushValueToArrayNode(arrayNode, -1, LAST, bits);
                pushValueToArrayNode(arrayNode, -2, LAST_BUT_ONE, bits);
                pushValueToArrayNode(arrayNode, -3, LAST_BUT_TWO, bits);
                node = arrayNode;
            }
        }
        return node;
    }

    @Override
    public JsonNode getNode() {
        if (isIgnored()) return null;
        ObjectNode res = JsonNodeFactory.instance.objectNode();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            JsonNode item = getNodeForDay(dayOfWeek.getValue() - 1);
            if (item != null) {
                res.set(dayOfWeek.toString(), item);
            }
        }
        return res;
    }

    private void handleDayAssignment(JsonNode node, int index) {
        if (node.isArray()) {
            for (int i = 0; i < node.size(); ++i) {
                int mask = node.get(i).asInt(-100);
                if (mask == 0 || mask < -3 || mask > 4) {
                    log.warn("Day of week index in month is invalid: {}", mask);
                } else {
                    dayOfWeekMasks[index] |= getBitMaskForIndex(mask > 0 ? mask - 1 : mask);
                }
            }
        } else {
            if (node.asText(VALUE_NONE).equalsIgnoreCase(VALUE_ALL)) {
                dayOfWeekMasks[index] = ALL;
            }
        }
    }

    private void fieldNameHandleOnSetNode(ObjectNode obj, String name) {
        int index;

        try {
            index = DayOfWeek.valueOf(name.toUpperCase()).getValue() - 1;
        } catch (Exception e) {
            log.warn("Days of week parse error. Undefined day : {}", name, e);
            throw e;
        }
        if (index >= 0 && index < 7) {
            handleDayAssignment(obj.get(name), index);
        }
    }

    @Override
    public void setNode(JsonNode node) {
        clear();
        try {
            if (node != null && node.isObject()) {
                ObjectNode obj = (ObjectNode) node;
                Iterator<String> fieldNames = obj.fieldNames();
                while (fieldNames.hasNext()) {
                    fieldNameHandleOnSetNode(obj, fieldNames.next());
                }
            }
        } catch (Exception e) {
            log.warn("JSON Node parse error.", e);
            throw e;
        }
    }
}
