package ua.com.solidity.pipeline;

@SuppressWarnings("unused")
public class PipelineFactory {
    private final PipelinePrototypeProvider provider;
    public PipelineFactory(PipelinePrototypeProvider provider) {
        this.provider = provider;
    }

    public Pipeline createPipeline(String jsonString) {
        return new Pipeline(provider, jsonString);
    }
}
