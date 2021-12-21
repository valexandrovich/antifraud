package ua.com.solidity.pipeline;

import com.fasterxml.jackson.databind.JsonNode;

public class PipelineFactory {
    private final PipelinePrototypeProvider provider;
    public PipelineFactory(PipelinePrototypeProvider provider) {
        this.provider = provider;
    }

    public Pipeline createPipeline(String jsonString) {
        return new Pipeline(provider, jsonString);
    }
    public Pipeline createPipelineByNode(JsonNode node) {return new Pipeline(provider, node); }
}
