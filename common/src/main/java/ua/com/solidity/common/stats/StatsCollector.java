package ua.com.solidity.common.stats;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public class StatsCollector extends StatsObject {
    public static final long DEFAULT_EXAMPLES_COUNT = 16;
    private final long maxExampleCount;
    private long errorCount = 0;

    public StatsCollector(long maxExampleCount) {
        super(null);
        this.maxExampleCount = maxExampleCount < 0 ? DEFAULT_EXAMPLES_COUNT : maxExampleCount;
    }

    @Override
    public void fillNode(ObjectNode node) {
        node.put("errorCount", errorCount);
        super.fillNode(node);
    }

    @Override
    protected void doClear() {
        super.doClear();
        errorCount = 0;
    }

    public final void pushError() {
        ++errorCount;
    }
}
