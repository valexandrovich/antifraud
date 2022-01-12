package ua.com.solidity.pipeline;

import org.springframework.context.ApplicationContext;

@SuppressWarnings("unused")
public class ContextPipelinePrototypeProvider implements PipelinePrototypeProvider {
    ApplicationContext context;
    String prefix;
    String suffix;
    public ContextPipelinePrototypeProvider(ApplicationContext context, String prefix, String suffix) {
        this.context = context;
        this.prefix = prefix == null ? "" : prefix.trim();
        this.suffix = suffix == null ? "" : suffix.trim();
    }

    @Override
    public Prototype getPrototype(String prototypeName) {
        String name = prefix + prototypeName + suffix;
        return context.getBeansOfType(Prototype.class).getOrDefault(name, null);
    }
}
