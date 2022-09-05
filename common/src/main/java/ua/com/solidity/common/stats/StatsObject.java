package ua.com.solidity.common.stats;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import ua.com.solidity.common.data.DataObject;

import java.util.HashMap;
import java.util.Map;

@Getter
public class StatsObject extends StatsItem {
    private final Map<String, StatsField> fields = new HashMap<>();

    protected StatsObject(StatsValue parent) {
        super(parent);
    }

    @SuppressWarnings("unused")
    public StatsObject() {
        super(null);
    }

    @Override
    protected void doClear() {
       fields.clear();
    }

    private void pushField(DataObject.FieldEntry entry) {
        StatsField field = fields.getOrDefault(entry.getName(), null);
        if (field == null) {
            field = new StatsField(this, entry.getName());
            fields.put(entry.getName(), field);
        }
        field.pushDataField(entry.getField());
    }

    public final void push(DataObject obj) {
        if (obj == null) {
            ++nullCount;
            return;
        }
        ++count;

        for (var fieldEntry : obj.getFields()) {
            pushField(fieldEntry);
        }
    }

    @Override
    protected void fillNode(ObjectNode node) {
        for (var fieldEntry : fields.entrySet()) {
            node.set("<" + fieldEntry.getKey() + ">", fieldEntry.getValue().getNode());
        }
    }
}
