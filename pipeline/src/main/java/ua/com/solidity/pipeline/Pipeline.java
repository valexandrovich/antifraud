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
    boolean executed = false;

    protected Pipeline(PipelinePrototypeProvider provider, String jsonString) {
        initialize(provider, jsonString);
    }

    protected Pipeline(PipelinePrototypeProvider provider, JsonNode node) {
        this.provider = provider;
        initializeItems(node);
    }

    private void initialize(PipelinePrototypeProvider provider, String jsonString) {
        this.provider = provider;
        try {
            initializeItems((new ObjectMapper()).readTree(jsonString));
        } catch (Exception e) {
            log.warn("Error during parse Pipeline Json", e);
            invalid = true;
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

    public final <T> T getItemData(String name, String value, Class<T> clazz) {
        Item item = itemByName.getOrDefault(name, null);
        return item == null ? null : item.getLocalData(value, clazz);
    }

    @SuppressWarnings("unused")
    public final <T> T getItemData(String name, String value, Class<T> clazz, T defaultValue) {
        T res = getItemData(name, value, clazz);
        return res == null ? defaultValue : res;
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
            item.prepareInputs();
            if (terminated) break;
        }
    }

    protected final void initializeItems(JsonNode node) {
        executed = invalid = terminated = false;
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
        if (!invalid) invalid = !topologicalSort();
        if (!invalid) {
            prepareDependencies();
        }
    }

    protected void addItem(Item node) {
        if (node == null) return;
        items.add(node);
        itemByName.put(node.name, node);
    }

    private boolean execList(Collection<? extends Item> items) {
        for (var a : items) {
            if (a.executionFailed()) return false;
        }
        return !terminated;
    }

    final boolean doExecute(Item item, boolean withItem) {
        if (terminated || item == null) return false;
        if (withItem) {
            if (item.executionFailed()) return false;
        }
        return execList(item.depAncestors) && execList(item.directDependencies);
    }

    public final boolean isValid() {
        return !invalid;
    }

    public final boolean execute() {
        terminated = invalid || executed;
        if (terminated) {
            log.warn("Pipeline is invalid or already executed. Execution terminated.");
            return true;
        }

        for (Item item : items) {
            item.completed = false;
            item.visited = false;
            terminated = item.beforePipelineExecution();
            if (terminated) {
                log.warn("Pipeline post initialization item failed {}.", item);
                return true;
            }
        }

        for (var item : items) {
            if (!doExecute(item, true)) {
                break;
            }
        }

        for (int i = items.size() - 1; i >= 0; --i) {
            items.get(i).doClose();
        }

        executed = true;
        return !terminated;
    }

    private void replaceItems(List<Item> order) {
        items = order;
        for (int i = 0; i < items.size(); ++i) {
            items.get(i).index = i;
        }
    }

    private boolean topologyValidate(List<Item> items) {
        for (Item item : items) {
            for (Input input : item.inputs) {
                if (input.inputItem.index >= item.index) {
                    log.error("Pipeline topology conflict between item input from {} and item {}", input.inputItem.name, item.name);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean topologicalSort() {
        List<Item> order = new ArrayList<>();
        for (Item item : items) {
            item.visited = false;
        }

        while (!items.isEmpty()) {
            doTopologicalSort(order, items.get(0));
        }

        if (topologyValidate(order)) {
            replaceItems(order);
            return true;
        }

        return false;
    }

    private void doTopologicalSort(List<Item> order, Item item) {
        item.visited = true;
        for (Input input : item.inputs) {
            if (!input.inputItem.visited) {
                doTopologicalSort(order, input.inputItem);
            }
        }
        item.index = order.size();
        order.add(item);
        items.remove(item);
    }

    private void prepareDependencies() {
        for (int i = items.size() - 1; i >= 0; --i) {
            Item item = items.get(i);
            for (Input input : item.inputs) {
                if (input.inputItem != null) {
                    input.inputItem.directDependencies.add(item);
                    input.inputItem.dependencies.add(item);
                    input.inputItem.dependencies.addAll(item.dependencies);
                }
            }
        }

        for (Item item : items) {
            item.directDependencies.sort(Comparator.comparingInt(i -> i.index));
            for (var dependant : item.directDependencies) {
                dependant.ancestors.add(item);
                dependant.ancestors.addAll(item.ancestors);
            }
        }

        Set<Item> deps = new HashSet<>();

        for (var item : items) {
            deps.clear();
            for (var dependant : item.directDependencies) {
                deps.addAll(dependant.ancestors);
            }
            item.dependencies.forEach(deps::remove);
            deps.removeAll(item.ancestors);
            deps.remove(item);
            item.depAncestors.addAll(deps);
            item.depAncestors.sort(Comparator.comparingInt(i -> i.index));
        }
        /*  uncomment this block for clear all ancestors
        for (var item : items) {
            item.ancestors.clear();
        }
        */
    }
}
