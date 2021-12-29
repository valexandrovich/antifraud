package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.OutputStats;
import ua.com.solidity.common.Utils;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

@Slf4j
public abstract class PPCustomDBWriter extends Prototype {
    public static final String OUTPUT_STATS = "outputStats";
    public static final String GROUP = "group";
    public static final String CACHE = "cache";
    public static final String DEFAULT_NAME = "undefined";
    public static final String INPUT = "input";
    @Override
    public Class<?> getOutputClass() {
        return null;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        OutputStats stats = item.getPipelineParam(OUTPUT_STATS, OutputStats.class);
        if (stats == null) {
            stats = new OutputStats();
            item.setPipelineParam(OUTPUT_STATS, stats);
        }
        String name = DEFAULT_NAME;
        if (node == null) {
            log.error("Source name is not defined for prototype {}.", this.getClass().getName());
            item.terminate();
            return;
        }
        if (node.isObject() && node.hasNonNull(GROUP) && node.get(GROUP).isTextual()) {
            name = node.get(GROUP).asText();
        } else {
            if (node.isTextual()) name = node.asText();
        }
        OutputCache cache = new OutputCache(stats.getSource(name), getObjectCacheSize(), getErrorCacheSize());
        item.setLocalData(CACHE, cache);
        item.mapInputs(INPUT, JsonNode.class);
    }

    private void internalFlushOutputObjects(Item item, OutputCache cache) {
        int committed = flushObjects(item, cache);
        int count = cache.getHandledObjectsCount();
        if (count > 0) {
            for (int i = 0; i < count; ++i) {
                item.yieldResult(cache.getHandledObject(i), false);
            }
        }
        cache.objectCacheHandled(committed);
    }

    private void internalFlushErrors(Item item, OutputCache cache) {
        int count;
        try {
            count = flushErrors(item, cache);
        } catch (Exception e) {
            count = 0;
        }
        cache.errorCacheHandled(count);
    }

    @SuppressWarnings("unused")
    protected Object getObjectInstance(Item item, JsonNode value) {
        return null;
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

    private void doExecuteOnNotEOF(Item item, Input input, OutputCache cache, JsonNode value) {
        cache.getGroup().incTotalRowCount();
        if (input.isError()) {
            try {
                ErrorReport report = Utils.jsonToValue(value, ErrorReport.class);
                if (cache.putError(report)) {
                      internalFlushErrors(item, cache);
                }
            } catch (Exception e) {
                log.error("Error report not valid.", e);
            }
        } else {
            Object instance = getObjectInstance(item, value);
            if (instance != null && cache.putObject(instance)) {
                internalFlushOutputObjects(item, cache);
            }
        }
    }

    private Object doExecute(Item item, Input input, OutputCache cache) {
        if (input.isBOF()) {
            doExecuteOnBOF(item, cache);
        }
        if (!item.terminated()) {
            if (input.hasData()) {
                doExecuteOnNotEOF(item, input, cache, input.getValue(JsonNode.class));
                item.stayUncompleted();
            } else {
                internalFlushOutputObjects(item, cache);
                internalFlushErrors(item, cache);
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

    protected abstract int getObjectCacheSize();

    protected abstract int getErrorCacheSize();

    protected abstract void beforeOutput(Item item, OutputCache cache);

    protected abstract void afterOutput(Item item, OutputCache cache);

    protected abstract int flushObjects(Item item, OutputCache cache);

    protected abstract int flushErrors(Item item, OutputCache cache);

    @Override
    protected void close(Item item) {
        // nothing yet
    }
}
