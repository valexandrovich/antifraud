package ua.com.solidity.common.stats;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataFieldType;

import java.util.EnumMap;

@Getter
public class StatsField extends StatsItem {
    private final String name;
    private final EnumMap<DataFieldType, StatsValue> values = new EnumMap<>(DataFieldType.class);

    public StatsField(StatsValue value) {
        super(value);
        name = null;
    }

    public StatsField(StatsObject statsObject, String name) {
        super(statsObject);
        this.name = name;
    }

    public final void pushDataField(DataField field) {
        DataFieldType type = DataField.getFieldType(field);
        if (type == DataFieldType.NULL) {
            ++nullCount;
            return;
        }
        ++count;
        StatsValue value = values.getOrDefault(type, null);
        if (value == null) {
            value = new StatsValue(this, type);
            values.put(type, value);
        }
        value.pushValue(field);
    }

    @Override
    protected void doClear() {
        for (var item : values.entrySet()) {
            item.getValue().clear();
        }
    }

    @Override
    protected void fillNode(ObjectNode node) {
        for (var value : values.entrySet()) {
            node.set('@' + value.getKey().name(), value.getValue().getNode());
        }
    }
}
