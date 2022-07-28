package ua.com.solidity.pipeline;

import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ContextPipelinePrototypeProvider implements PipelinePrototypeProvider {
    ApplicationContext context;
    String prefix;
    String suffix;
    final Map<String, Prototype> predeclared = new HashMap<>();
    public ContextPipelinePrototypeProvider(ApplicationContext context, String prefix, String suffix) {
        this.context = context;
        this.prefix = prefix == null ? "" : prefix.trim();
        this.suffix = suffix == null ? "" : suffix.trim();
        Map<String, Prototype> beans = context.getBeansOfType(Prototype.class);
        for (var entry : beans.entrySet()) {
            Class<?> cl = entry.getValue().getClass();
            if (cl.isAnnotationPresent(PipelinePrototype.class)) {
                PipelinePrototype pp = cl.getAnnotation(PipelinePrototype.class);
                predeclared.put(pp.value(), entry.getValue());
            }
        }
    }

    @Override
    public Prototype getPrototype(String prototypeName) {
        Prototype pt = predeclared.getOrDefault(prototypeName, null);
        if (pt != null) return pt;

        String name = prefix + prototypeName + suffix;
        return context.getBeansOfType(Prototype.class).getOrDefault(name, null);
    }
}
