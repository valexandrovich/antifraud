package ua.com.solidity.common.stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.common.ActionObject;
import ua.com.solidity.common.DurationPrinter;
import ua.com.solidity.common.Utils;
import ua.com.solidity.pipeline.Pipeline;
import ua.com.solidity.pipeline.PipelineFactory;

import java.io.File;

@Getter
@Setter
@CustomLog
public class StatsAction extends ActionObject {
    public static PipelineFactory pipelineFactory;

    private String inputFile;
    private String outputFile;
    private ArrayNode pipeline;

    @JsonIgnore
    private File input;

    @JsonIgnore
    private File output;

    @JsonIgnore
    private boolean pipelineChanged = false;

    @JsonIgnore
    private ObjectNode root = null;

    @SuppressWarnings("unused")
    private ArrayNode rebuildPipeline(ArrayNode pipeline, String attachName) {
        return pipeline;
    }

    @Override
    protected boolean doValidate() {
        input = Utils.getFileFromNFSFolder(inputFile, true);
        output = Utils.getFileFromNFSFolder(outputFile, false);
        if (input == null || output == null || pipeline == null || pipelineFactory == null) return false;

        if (!pipelineChanged) {
            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            statsNode.put("prototype", "StatsCollector");
            statsNode.put("name", "stats");
            ObjectNode inputs = JsonNodeFactory.instance.objectNode();
            inputs.put("input", "parser");
            statsNode.set("inputs", inputs);
            ObjectNode data = JsonNodeFactory.instance.objectNode();
            data.put("outputFile", output.getAbsolutePath());
            statsNode.set("data", data);
            pipeline.add(statsNode);
            root = JsonNodeFactory.instance.objectNode();
            root.set("pipeline", pipeline);
            pipelineChanged = true;
        }
        return true;
    }

    @Override
    @SuppressWarnings("Duplicates")
    protected boolean doExecute() {
        DurationPrinter elapsedTime = new DurationPrinter();

        acknowledge();

        Pipeline p = pipelineFactory.createPipelineByNode(root);
        if (p == null || !p.isValid()) {
            log.error("Pipeline is invalid.");
            return false;
        }

        p.setParam("FileName", input.getAbsolutePath());

        log.info("Statistics collection started");

        try {
            boolean res = p.execute();
            if (!res) {
                log.error("Pipeline execution failed.");
            } else {
                log.info("Pipeline execution completed.");
            }
        } catch (Exception e) {
            log.error("Error due pipeline execution.", e);
            return false;
        }

        elapsedTime.stop();

        log.info("Statistic collection completed. {}", elapsedTime.getDurationString());

        return true;
    }
}
