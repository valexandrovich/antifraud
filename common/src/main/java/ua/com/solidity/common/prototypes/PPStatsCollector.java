package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.data.DataBatch;
import ua.com.solidity.common.stats.StatsCollector;
import ua.com.solidity.pipeline.Input;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.File;

@CustomLog
public class PPStatsCollector extends Prototype {
    private static final String INPUT = "input";
    @Getter
    @Setter
    private static class Data {
        final StatsCollector collector;
        final File outputFile;
        Input input;

        public Data(File outputFile, long maxExampleCount) {
            this.collector = new StatsCollector(maxExampleCount);
            this.outputFile = outputFile;
        }

        public boolean setInput(Input input) {
            if (input == null) return false;
            this.input = input;
            return true;
        }
    }

    @Override
    public Class<?> getOutputClass() {
        return null;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        String outputFile = node.get("outputFile").asText(null);
        if (outputFile == null) {
            item.terminate();
            return;
        }

        File out = Utils.getFileFromNFSFolder(outputFile, false);

        if (out == null) {
            item.terminate();
            return;
        }

        long maxExampleCount = node.has("maxExampleCount") ? node.get("maxExampleCount").asLong(-1L) : -1L;

        item.mapInputs(INPUT, DataBatch.class);

        item.setInternalData(new Data(out, maxExampleCount));
    }

    @Override
    protected void beforePipelineExecution(Item item) {
        super.beforePipelineExecution(item);
        Data data = item.getInternalData(Data.class);
        if (data == null || !data.setInput(item.getInput(INPUT, 0))) {
            log.error("Statistic Input ({}) not assigned.", INPUT);
            item.terminate();
        }
    }

    private void handleBatch(Data data) {
        DataBatch batch = data.input.getValue(DataBatch.class);
        if (batch != null) {
            batch.handle(obj->{data.collector.push(obj); return null;},
                    err->{data.collector.pushError(); return true;});
        }
    }

    @Override
    protected Object execute(@NonNull Item item) {
        Data data = item.getInternalData(Data.class);
        if (data.input.isIterator()) {
            if (data.input.hasData()) {
                handleBatch(data);
            }
        } else {
            handleBatch(data);
        }
        return null;
    }

    @Override
    protected void close(Item item) {
        Data data = item.getInternalData(Data.class);
        JsonNode node = data.collector.getNode();
        boolean res = Utils.writeJsonNodeToFile(data.outputFile, node);
        if (!res) {
            log.info("Can't write stats data to file {}", data.outputFile.getAbsolutePath());
        }
    }
}
