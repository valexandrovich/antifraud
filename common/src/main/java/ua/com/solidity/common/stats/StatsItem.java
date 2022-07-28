package ua.com.solidity.common.stats;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class StatsItem {
    @Getter
    private StatsItem parent;
    private final List<StatsItem> items = new ArrayList<>();
    @Getter
    protected long count = 0;
    @Getter
    protected long nullCount = 0;

    public StatsItem(StatsItem parent) {
        this.parent = parent;
        if (parent != null) {
            parent.items.add(this);
        }
    }

    private void internalClear() {
        for (var item: items) {
            item.clear();
        }
        items.clear();
        parent = null;
        count = 0;
        nullCount = 0;
    }

    protected void doClear() {
        // nothing yet
    }

    public final void clear() {
        internalClear();
        doClear();
    }

    protected final StatsItem getRoot() {
        return parent == null ? this : parent.getRoot();
    }

    protected void fillNode(ObjectNode node) {
        // nothing yet
    }

    public final ObjectNode getNode() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        if (count > 0) {
            node.put("valueCount", count);
        }

        if (nullCount > 0) {
            node.put("nullCount", nullCount);
        }
        fillNode(node);
        return node;
    }
}
