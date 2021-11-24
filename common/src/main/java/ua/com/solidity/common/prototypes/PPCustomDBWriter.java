package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.ErrorReport;
import ua.com.solidity.common.OutputStats;
import ua.com.solidity.common.Utils;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

@Slf4j
public abstract class PPCustomDBWriter extends Prototype {
    public static final String OUTPUT_STATS = "outputStats";
    public static final String GROUP = "group";
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
            OutputStats.Group group = stats.getSource(name);
            item.setLocalData(GROUP, group);
        }

        item.mapInputs(INPUT, JsonNode.class);
    }

    private void doExecuteOnBOF(Item item, OutputStats.Group group) {
        group.clear();
        try {
            beforeOutput(item);
        } catch (Exception e) {
            log.error("Error on beforeOutput for source {}", group.getName(), e);
            item.terminate();
        }
    }

    private void doExecuteOnNotEOF(Item item, Input input, OutputStats.Group group, JsonNode value) {
        if (input.isError()) {
            group.incParseErrorCount();
            try {
                outputError(item, Utils.jsonToValue(value, ErrorReport.class));
            } catch (Exception e) {
                group.incInsertErrorInfoCount();
            }
        } else {
            try {
                outputObject(item, value);
                group.incInsertCount();
            } catch (Exception e) {
                group.incInsertErrorCount();
            }
        }
    }

    private void doExecute(Item item, Input input, OutputStats.Group group) {
        group.incTotalRowCount();

        if (input.isBOF()) {
            doExecuteOnBOF(item, group);
        }

        if (!input.isEOF()) {
            doExecuteOnNotEOF(item, input, group, input.getValue(JsonNode.class));
        } else {
            try {
                afterOutput(item);
            } catch (Exception e) {
                log.error("Error on afterOutput for source {}", group.getName(), e);
            }
        }
    }

    @Override
    protected Object execute(@NonNull Item item) {
        Input input = item.getInput(INPUT, 0);
        OutputStats.Group group = item.getLocalData(GROUP, OutputStats.Group.class);

        if (input != null && input.isIterator()) {
            doExecute(item, input, group);
        }

        return null;
    }

    protected abstract void beforeOutput(Item item);

    protected abstract void afterOutput(Item item);

    protected abstract void outputObject(Item item, JsonNode node);

    protected abstract void outputError(Item item, ErrorReport error);

    @Override
    protected void close(Item item) {
        // nothing yet
    }
}
