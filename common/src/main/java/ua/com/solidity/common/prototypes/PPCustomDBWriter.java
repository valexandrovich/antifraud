package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.OutputStats;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

@Slf4j
public abstract class PPCustomDBWriter extends Prototype {
    public static final String OUTPUT_STATS = "outputStats";
    public static final String SOURCE = "source";
    public static final String GROUP = "group";
    public static final String CACHE = "cache";
    public static final String DEFAULT_NAME = "(undefined)";
    public static final String INPUT = "input";

    @Override
    public Class<?> getOutputClass() {
        return null;
    }

    public Class<?> getInputClass() {
        return DataBatch.class;
    }

    private void initCache(OutputStats stats, Item item) {
        String name = item.getLocalData(GROUP, String.class, DEFAULT_NAME);
        OutputCache cache = new OutputCache(stats.getGroup(name));
        item.setLocalData(CACHE, cache);
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        item.setLocalData(GROUP, node.hasNonNull(GROUP) && node.get(GROUP).isTextual() ? node.get(GROUP).asText() : DEFAULT_NAME);
        item.mapInputs(INPUT, getInputClass());
    }

    @Override
    protected void beforePipelineExecution(Item item) {
        OutputStats stats = item.getPipelineParam(OUTPUT_STATS, OutputStats.class);
        if (stats == null) {
            String source = item.getPipelineParam(SOURCE, String.class);
            stats = new OutputStats(source == null ? DEFAULT_NAME : source);
            item.setPipelineParam(OUTPUT_STATS, stats);
        }
        initCache(stats, item);
    }

    private void doExecuteOnBOF(Item item, OutputCache cache) {
        cache.getGroup().clear();
        item.yieldBegin();
        try {
            beforeOutput(item, cache);
        } catch (Exception e) {
            log.error("Error on beforeOutput for source {}", cache.getGroup().getName(), e);
            item.terminate();
        }
    }

    private void doExecuteOnNotEOF(Item item, OutputCache cache, DataBatch batch) {
        if (batch == null) return;
        cache.put(batch);
        cache.batchHandled(flushObjects(item, cache));
    }

    private Object doExecute(Item item, Input input, OutputCache cache) {
        if (input.isBOF()) {
            doExecuteOnBOF(item, cache);
        }
        if (!item.terminated()) {
            if (input.hasData()) {
                doExecuteOnNotEOF(item, cache, input.getValue(DataBatch.class));
                item.stayUncompleted();
            } else {
                try {
                    afterOutput(item, cache);
                } catch (Exception e) {
                    log.error("Error on afterOutput for source {}", cache.getGroup().getName(), e);
                }
            }
        }

        return null;
    }

    @Override
    protected Object execute(@NonNull Item item) {
        Input input = item.getInput(INPUT, 0);
        OutputCache cache = item.getLocalData(CACHE, OutputCache.class);
        Object res = null;
        if (input != null && input.isIterator()) {
            res = doExecute(item, input, cache);
        }

        return res;
    }

    protected final OutputCache getOutputCache(@NonNull Item item) {
        return item.getLocalData(CACHE, OutputCache.class);
    }

    protected abstract void beforeOutput(Item item, OutputCache cache);

    protected abstract void afterOutput(Item item, OutputCache cache);

    protected abstract int flushObjects(Item item, OutputCache cache);

    @Override
    protected void close(Item item) {
        // nothing yet
    }
}
