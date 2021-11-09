package ua.com.solidity.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Pipeline {
    private static final String KEY_PIPELINE = "pipeline";
    private static final String KEY_PROTOTYPE = "prototype";
    private static final String KEY_NAME = "name";
    private static final String KEY_INPUTS = "inputs";
    private static final String KEY_DATA = "data";

    List<Item> items = new ArrayList<>();
    final Map<String, Item> itemByName = new HashMap<>();
    private PipelinePrototypeProvider provider;
    private Map<String, Object> params;
    boolean terminated = false;
    boolean invalid = false;

    protected Pipeline(PipelinePrototypeProvider provider, String jsonString) {
        initialize(provider, jsonString);
    }

    private void initialize(PipelinePrototypeProvider provider, String jsonString) {
        this.provider = provider;
        try {
            initializeItems((new ObjectMapper()).readTree(jsonString));
        } catch (Exception e) {
            log.warn("Error during parsing Pipeline Json", e);
        }
    }

    @SuppressWarnings("unused")
    public final void setParam(String name, Object value) {
        if (params == null) params = new HashMap<>();
        params.put(name, value);
    }

    @SuppressWarnings("UnusedReturnValue")
    public final <T> T getParam(String name, Class<? extends T> clazz) {
        return params == null ? null : clazz.cast(getParam(name));
    }

    public final Object getParam(String name) {
        return params == null ? null : params.getOrDefault(name, null);
    }

    private List<String> handleParsedInput(JsonNode value) {
        List<String> res = new ArrayList<>();
        if (value != null) {
            if (value.isArray()) {
                for (int i = 0; i < value.size(); ++i) {
                    res.add(value.get(i).asText());
                }
            } else if (value.isTextual()) {
                res.add(value.asText());
            } else {
                log.warn("Illegal reference type on 'inputs' field {}", value.asText());
            }
        }
        return res;
    }

    private Map<String, List<String>> parseInputs(JsonNode node) {
        Map<String, List<String>> res = new HashMap<>();
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                res.put(entry.getKey(), handleParsedInput(entry.getValue()));
            }
        }
        return res;
    }

    private void createItem(JsonNode node) {
        String className = node.hasNonNull(KEY_PROTOTYPE) ? node.get(KEY_PROTOTYPE).asText() : null;
        String name = node.hasNonNull(KEY_NAME) ? node.get(KEY_NAME).asText() : null;
        Map<String, List<String>> inputs = node.hasNonNull(KEY_INPUTS) ? parseInputs(node.get(KEY_INPUTS)) : null;
        JsonNode data = node.hasNonNull(KEY_DATA) ? node.get(KEY_DATA) : null;

        if (provider != null) {
            Prototype prototype = provider.getPrototype(className);
            if (prototype == null) {
                log.warn("Pipeline Item Factory not found for name {}", className);
            } else {
                prototype.createInstance(this, name, inputs, data);
            }
        } else {
            log.warn("Pipeline Item Factory Provider is null.");
        }
    }

    private void prepareItems() {
        for (Item item : items) {
            item.visited = false;
            item.prepareJoints();
            if (terminated) break;
        }
    }

    protected final void initializeItems(JsonNode node) {
        invalid = terminated = false;
        if (node == null) return;
        if (node.isObject() && node.hasNonNull(KEY_PIPELINE)) {
            node = node.get(KEY_PIPELINE);
            if (!node.isArray()) return;
            for (int i = 0; i < node.size(); ++i) {
                createItem(node.get(i));
            }
        }
        prepareItems();
        invalid = terminated;
        if (!invalid) {
            topologicalSort();
            prepareDependencies();
        }
    }

    protected void addItem(Item node) {
        if (node == null) return;
        items.add(node);
        itemByName.put(node.name, node);
    }

    final void doExecute(int from) {
        boolean handled;
        do {
            handled = false;
            for (int i = from; i < items.size(); ++i) {
                Item item = items.get(i);
                if (item.tryToExecute()) {
                    handled = true;
                }
            }
        } while (handled && !terminated);
    }

    public final boolean isValid() {
        return !invalid;
    }

    public final boolean execute() {
        terminated = invalid;
        if (terminated) {
            log.warn("Pipeline is invalid. Execution terminated.");
            return true;
        }

        for (Item item : items) {
            item.completed = false;
            item.visited = false;
        }

        doExecute(0);

        for (Item item : items) {
            try {
                item.doClose();
            } catch (Exception e) {
                log.warn("Can't close item {}", item.name);
                terminated = true;
            }
            item.closed = true;
        }
        return !terminated;
    }

    private void replaceItems(List<Item> order) {
        items = order;
        for (int i = 0; i < items.size(); ++i) {
            items.get(i).index = i;
        }
    }

    private void topologicalSort() {
        List<Item> order = new ArrayList<>();
        for (Item item : items) {
            item.visited = false;
        }

        while (!items.isEmpty()) {
            doTopologicalSort(order, items.get(0));
        }

        replaceItems(order);
    }

    private void doTopologicalSort(List<Item> order, Item item) {
        item.visited = true;
        for (Input input : item.inputs) {
            if (!input.inputItem.visited) {
                doTopologicalSort(order, input.inputItem);
            }
        }
        order.add(item);
        items.remove(item);
    }

    private void prepareDependencies() {
        for (int i = items.size() - 1; i >= 0; --i) {
            Item item = items.get(i);
            for (Input input : item.inputs) {
                input.inputItem.dependencies.add(item);
                input.inputItem.dependencies.addAll(item.dependencies);
            }
        }
    }
}
