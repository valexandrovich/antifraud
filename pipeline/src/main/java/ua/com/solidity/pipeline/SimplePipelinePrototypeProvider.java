package ua.com.solidity.pipeline;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SimplePipelinePrototypeProvider implements PipelinePrototypeProvider {
    private final Map<String, Prototype> prototypes = new HashMap<>();

    public final SimplePipelinePrototypeProvider add(String name, Prototype prototype) {
        if (prototype != null) prototypes.put(name, prototype);
        return this;
    }

    @Override
    public Prototype getPrototype(String prototypeName) {
        return prototypes.getOrDefault(prototypeName, null);
    }
}
