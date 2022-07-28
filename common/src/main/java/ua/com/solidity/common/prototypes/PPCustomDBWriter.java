package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.NonNull;
import ua.com.solidity.common.OtpExchange;
import ua.com.solidity.common.OutputCache;
import ua.com.solidity.common.OutputStats;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.model.EnricherPortionMessage;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.util.UUID;


@CustomLog
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
        //old variant: item.setLocalData(GROUP, node.hasNonNull(GROUP) && node.get(GROUP).isTextual() ? node.get(GROUP).asText() : DEFAULT_NAME);
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

    protected void changeStatus(Item item, OutputCache cache) {

        // nothing yet
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
        boolean selfPortionID = batch.getPortion() == null;
        if (selfPortionID) {
            batch.setPortion(UUID.randomUUID());
        }
        flushObjects(item, cache);
        cache.batchHandled();
        changeStatus(item, cache);
        batch.pack();
        if (batch.getObjectCount() > 0) {
            item.yieldResult(batch, false);
        }

        if (selfPortionID) {
            Utils.sendRabbitMQMessage(OtpExchange.ENRICHER,
                    new EnricherPortionMessage(getTableName(item), batch.getPortion()));
            batch.setPortion(null);
        }
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

    protected abstract String getTableName(Item item);

    protected abstract void beforeOutput(Item item, OutputCache cache);

    protected abstract void afterOutput(Item item, OutputCache cache);

    protected abstract void flushObjects(Item item, OutputCache cache);

    @Override
    protected void close(Item item) {
        // nothing yet
    }
}
