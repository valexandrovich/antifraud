package ua.com.solidity.common.stats;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import ua.com.solidity.common.data.DataArray;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataFieldType;
import ua.com.solidity.common.data.DataObject;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StatsValue extends StatsItem {
    private final long maxExampleCount;
    private final DataFieldType fieldType;
    private long maxSize = 0;
    private StatsObject collector = null;
    private StatsField arrayField = null;
    private final List<String> examples = new ArrayList<>();

    public StatsValue(StatsField field, DataFieldType fieldType) {
        super(field);
        StatsItem root = getRoot();
        if (root instanceof StatsCollector) {
            maxExampleCount = ((StatsCollector) root).getMaxExampleCount();
        } else {
            maxExampleCount = StatsCollector.DEFAULT_EXAMPLES_COUNT;
        }

        this.fieldType = fieldType;
        if (fieldType == DataFieldType.OBJECT) {
            collector = new StatsObject(this);
        } else if (fieldType == DataFieldType.ARRAY) {
            arrayField = new StatsField(this);
        }
    }

    @Override
    protected void doClear() {
        examples.clear();
        collector = null;
        arrayField = null;
    }

    private void handleValue(String value) {
        if (value == null || value.isBlank()) {
            ++nullCount;
            return;
        }
        ++count;

        if (value.length() > maxSize) {
            maxSize = value.length();
        }

        if (examples.size() >= maxExampleCount || examples.contains(value)) return;
        examples.add(value);
    }

    private void handleObject(DataObject obj) {
        if (obj == null) {
            ++nullCount;
            return;
        }
        ++count;
        collector.push(obj);
    }

    private void handleArray(DataArray arr) {
        if (arr == null) {
            ++nullCount;
            return;
        }
        ++count;
        long size = arr.getArrayLength();

        if (maxSize < size) {
            maxSize = size;
        }

        for (var item: arr.items()) {
            arrayField.pushDataField(item);
        }
    }

    public final void pushValue(DataField field) {
        if (DataField.getFieldType(field) == fieldType) {
            switch (fieldType) {
                case OBJECT:
                    handleObject(DataField.getObject(field));
                    break;
                case ARRAY:
                    handleArray(DataField.getArray(field));
                    break;
                default:
                    handleValue(DataField.getString(field));
                    break;
            }
        }
    }

    @Override
    protected void fillNode(ObjectNode node) {
        if (maxSize > 0) {
            node.put("maxSize", maxSize);
        }

        getExamples(node);

        switch (fieldType) {
            case OBJECT:
                node.set("{INSTANCE}", collector.getNode());
                break;
            case ARRAY:
                node.set("{ARRAY-ITEM}", arrayField.getNode());
                break;
            default:
                break;
        }
    }

    protected void getExamples(ObjectNode target) {
        if (examples.size() > 0) {
            ArrayNode examplesNode = JsonNodeFactory.instance.arrayNode();
            for (var example : examples) {
                examplesNode.add(example);
            }
            target.set("examples:", examplesNode);
        }
    }
}
