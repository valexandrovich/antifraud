package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.BitSet;

@Slf4j
@Getter
@Setter
public abstract class Param implements CustomParam {

    public static final String KEY_TYPE = "type";
    public static final String KEY_VALUE = "value";

    @Setter(AccessLevel.NONE)
    private int minIndex;
    @Setter(AccessLevel.NONE)
    public int maxIndex;
    private int period; // 1..max, 0 - unused value
    private int once; // min .. max
    protected BitSet bitSet;
    @Setter(AccessLevel.NONE)
    private boolean acceptPeriod;

    private Activation activation = Activation.IGNORED;

    protected Param() {
        initialize();
    }

    protected abstract void initialize();

    protected final void doInitialize(int minIndex, int maxIndex, boolean acceptPeriod) {
        if (minIndex > 0) minIndex = 0;
        if (maxIndex < 1) maxIndex = 1;
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        this.acceptPeriod = acceptPeriod;
        this.bitSet = new BitSet();
    }
    @Override
    public final boolean isIgnored() {
        return activation == Activation.IGNORED;
    }
    public final boolean isPeriodic() {
        return activation == Activation.PERIODIC;
    }
    public final int getPeriod() {
        return isPeriodic() ? period : -1;
    }

    protected final boolean validateIndex(int index) {
        return index >= minIndex && index <= maxIndex;
    }

    public final int errorIndex() {
        return getMinIndex() - 1;
    }

    protected int zoneCheck(int[] indexes, int indexCount) {
        indexes[2] = 0;
        return indexCount;
    }

    private boolean validateOnceValue(int[] indexes, int indexCount) {
        return indexes[0] == once || (indexCount > 1 && indexes[1] == once) ||
                (indexCount > 2 && indexes[2] == once);
    }

    private boolean validateSetValue(int[] indexes, int indexCount) {
        return bitSet.get(indexes[0] - minIndex) || (indexCount > 1 && bitSet.get(indexes[1] - minIndex)) ||
                (indexCount > 2 && bitSet.get(indexes[2] - minIndex));
    }

    public final boolean invalidValue(int index, int count) { // index - test, count - real items count
        if (index < 0 || index >= count || count <= maxIndex) return true; // count must be > then mMaxIndex

        if (activation == Activation.IGNORED) return false;
        if (activation == Activation.PERIODIC) return true;

        // some indexes has mirror negative values

        int[] indexes = {0, 0, 0}; // third index for future purposes (Zone check)
        int indexCount = 0;

        if (validateIndex(index)) indexes[indexCount++] = index;
        if (validateIndex(index - count)) indexes[indexCount++] = index - count;
        indexCount = zoneCheck(indexes, indexCount);

        if (indexCount == 0) return true;

        if (activation == Activation.ONCE) {
            return !validateOnceValue(indexes, indexCount);
        }

        return !validateSetValue(indexes, indexCount);
    }

    public final void setOnce(int index) {
        if (!validateIndex(index)) return;
        clear();
        once = index;
        activation = Activation.ONCE;
    }

    public final void clear() { // virtual
        period = 0;
        once = 0;
        if (bitSet != null) bitSet.clear();
        activation = Activation.IGNORED;
    }

    public final void setPeriod(int value) {
        if (!acceptPeriod || value <= 0) return;
        clear();
        period = value;
        activation = Activation.PERIODIC;
    }

    public final boolean isIndexSet(int index) {
        if (validateIndex(index) &&  !isIgnored()) {
            if (activation == Activation.ONCE) {
                return index == once;
            } else if (activation == Activation.SET) {
                return bitSet.get(index);
            }
        }
        return false;
    }

    public final void addIndex(int index) {
        if (!validateIndex(index)) return;
        if (activation == Activation.IGNORED) {
            setOnce(index);
        } else if (activation == Activation.ONCE) {
            if (once != index) {
                bitSet.set(once);
                activation = Activation.SET;
            }
        } else if (activation != Activation.SET) return;

        bitSet.set(index);
    }

    protected JsonNode convertPeriodToNode(int period) {
        return JsonNodeFactory.instance.numberNode(period);
    }

    protected int convertNodeToPeriod(JsonNode node) {
        return node.asInt(0);
    }

    protected JsonNode convertIndexToNode(int index) {
        return JsonNodeFactory.instance.numberNode(index >= 0 ? index + 1 : index);
    }

    protected int convertNodeToIndex(JsonNode node) {
        if (node != null && node.isNumber()) {
            int value = node.asInt();
            if (value > 0) --value;
            else if (value == 0) value = errorIndex();
            return value;
        }
        return errorIndex();
    }

    protected final ArrayNode getListOfItems() {
        ArrayNode res = JsonNodeFactory.instance.arrayNode();
        for (int i = 0; i < maxIndex; ++i) {
            if (isIndexSet(i)) {
                res.add(convertIndexToNode(i));
            }
        }
        for (int i = -1; i >= minIndex; --i) {
            if (isIndexSet(i)) {
                res.add(convertIndexToNode(i));
            }
        }

        return res;
    }

    protected final void setListOfItems(ArrayNode values) {
        clear();
        for (int i = 0; i < values.size(); ++i) {
            addIndex(convertNodeToIndex(values.get(i)));
        }
    }

    protected JsonNode getValueNode() {
        JsonNode res;
        if (activation == Activation.ONCE) {
            res = convertIndexToNode(once);
        } else if (activation == Activation.PERIODIC) {
            res = convertPeriodToNode(period);
        } else res = getListOfItems();
        return res;
    }

    protected void setValueNode(JsonNode node) {
        if (activation == Activation.ONCE) {
            setOnce(convertNodeToIndex(node));
        } else if (activation == Activation.PERIODIC) {
            setPeriod(convertNodeToPeriod(node));
        } else if (activation == Activation.SET && node.isArray()) {
            setListOfItems((ArrayNode) node);
        } else activation = Activation.IGNORED;
    }

    @Override
    public JsonNode getNode() {
        if (activation == Activation.IGNORED) return null;
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put(KEY_TYPE, activation.toString());
        node.set(KEY_VALUE, getValueNode());
        return node;
    }

    @Override
    public void setNode(JsonNode node) {
        clear();
        if (node == null) return;
        if (!node.isObject()) {
            log.warn("Schedule.Param JsonNode must be an object: {}", node);
            return;
        }
        ObjectNode obj = (ObjectNode) node;

        try {
            activation = Activation.valueOf(obj.get(KEY_TYPE).asText().toUpperCase());
        } catch (Exception e) {
            log.warn("Param parse error. invalid type: {}", node.getNodeType());
            return;
        }

        setValueNode(obj.get(KEY_VALUE));
    }
}
