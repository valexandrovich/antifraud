package ua.com.solidity.common.stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.*;
import java.util.function.Function;

@Getter
@Setter
@CustomLog
public class StatsAction extends ActionObject {
    @Getter
    @Setter
    private static PipelineFactory pipelineFactory;

    @Getter
    @Setter
    private static Function<Object, ArrayNode> pipelineGetter;

    private String inputFile;
    private String outputFile;
    private ArrayNode pipeline;
    private String source;
    private int sourceId = 0;
    private String parser = "parser";

    @JsonIgnore
    private File input;

    @JsonIgnore
    private File output;

    @JsonIgnore
    private boolean pipelineChanged = false;

    @JsonIgnore
    private ObjectNode root = null;

    private void rebuildPipeline() {
        Set<String> cache = new HashSet<>();
        if (findParser(cache)) {
            var iterator = pipeline.elements();
            while (iterator.hasNext()) {
                JsonNode node = iterator.next();
                if (!node.isObject() || !node.has("name") || !cache.contains(node.get("name").asText(null))) {
                    iterator.remove();
                }
            }
        }
    }

    private ObjectNode findNodeByName(String name) {
        for (var obj : pipeline) {
            if (obj instanceof ObjectNode && obj.has("name") &&
                    obj.get("name").isTextual() &&
                    obj.get("name").asText().equals(name)
            ) {
                return (ObjectNode) obj;
            }
        }
        return null;
    }

    private boolean findParser(Set<String> cache) {
        ObjectNode res = findNodeByName(parser);
        if (res == null) return false;
        if (res.has("prototype") && res.get("prototype").isTextual() &&
                res.get("prototype").asText("").endsWith("Parser")) {
            cache.add(parser);
            handleParser(res, cache);
            return true;
        }
        return false;
    }

    private void handleParser(ObjectNode node, Set<String> cache) {
        processInputs(node, cache, v-> Objects.equals(v, "stream"));
    }

    private void processInputs(ObjectNode node, Set<String> cache, Function<String, Boolean> handler) {
        if (!node.has("inputs") || handler == null) return;
        JsonNode inputs = node.get("inputs");
        if (!inputs.isObject()) return;
        ObjectNode inputsObject = (ObjectNode) inputs;
        var iterator = inputsObject.fields();
        while (iterator.hasNext()) {
            var item = iterator.next();
            if (!handler.apply(item.getKey())) {
                iterator.remove();
            } else {
                handleInput(iterator, item.getValue(), cache);
            }
        }
    }

    private boolean canRemoveReference(String name, Set<String> cache) {
        if (name != null) {
            ObjectNode target = findNodeByName(name);
            if (target != null) {
                cache.add(name);
                processInputs(target, cache, v->true);
                return false;
            }
        }
        return true;
    }

    private void handleInput(Iterator<?> externalIterator, JsonNode value, Set<String> cache) {
        if (value.isArray()) {
            ArrayNode arr = (ArrayNode) value;
            var iterator = arr.elements();
            while (iterator.hasNext()) {
                JsonNode element = iterator.next();
                if (element.isTextual()) {
                    String nodeName = element.asText(null);
                    if (canRemoveReference(nodeName, cache)) {
                        iterator.remove();
                    }
                }
            }
            if (arr.isEmpty()) {
                externalIterator.remove();
            }
        } else {
            String nodeName = value.asText(null);
            if (canRemoveReference(nodeName, cache)) {
                externalIterator.remove();
            }
        }
    }

    @Override
    protected boolean doValidate() {
        input = Utils.getFileFromNFSFolder(inputFile, true);
        output = Utils.getFileFromNFSFolder(outputFile, false);
        if (input == null || output == null || pipelineFactory == null ||
                pipeline == null && (source == null || source.isBlank()) && sourceId <= 0) return false;

        if (pipeline == null && pipelineGetter == null) return false;

        if (pipeline == null) {
            pipeline = pipelineGetter.apply(source == null ? sourceId : source);
        }

        if (pipeline == null) return false;

        rebuildPipeline();

        if (pipeline.isEmpty()) return false;

        ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
        statsNode.put("prototype", "StatsCollector");
        statsNode.put("name", "stats");
        ObjectNode inputs = JsonNodeFactory.instance.objectNode();
        inputs.put("input", parser);
        statsNode.set("inputs", inputs);
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put("outputFile", output.getAbsolutePath());
        statsNode.set("data", data);
        pipeline.add(statsNode);
        root = JsonNodeFactory.instance.objectNode();
        root.set("pipeline", pipeline);

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
