package ua.com.solidity.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.util.*;

@Slf4j
public final class Item {
    static final int FLAG_ITERATOR = 1;
    static final int FLAG_BOF = 2;
    static final int FLAG_EOF = 4;
    static final int FLAG_ERROR = 8;

    public final Prototype prototype;
    public final String name;
    final Pipeline pipeline;
    Map<String, List<Input>> jointSet = new HashMap<>();
    Map<String, List<String>> inputJointsDesc;
    final List<Input> inputs = new ArrayList<>();
    final Set<Item> dependencies = new HashSet<>();
    final Map<String, Object> localData = new HashMap<>();
    boolean visited = false;
    boolean completed = false;
    boolean closed = false;
    int flags = 0;
    int index = -1;
    Object outputValue = null;
    Object internalData = null;

    private Item(Prototype prototype, Pipeline pipeline, String name, Map<String, List<String>> inputs, JsonNode node) {
        this.prototype = prototype;
        this.pipeline = pipeline;
        this.name = name;
        this.inputJointsDesc = inputs;
        if (pipeline != null && prototype != null) {
            pipeline.addItem(this);
            prototype.initialize(this, node);
        }
    }

    static void createItem(Prototype prototype, Pipeline pipeline, String name, Map<String, List<String>> inputs, JsonNode node) {
        new Item(prototype, pipeline, name, inputs, node);
    }

    Class<?> getOutputClass() {
        return prototype.getOutputClass();
    }

    public <T> void mapInputs(String nameOfSet, Class<? extends T> clazz) {
        List<String> items = inputJointsDesc == null ? null : inputJointsDesc.getOrDefault(nameOfSet, null);
        if (items != null && !items.isEmpty()) {
            for (String item : items) {
                addInput(nameOfSet, item, clazz);
            }
        }
    }

    public void setInternalData(Object value) {
        internalData = value;
    }

    public <T> T getInternalData(Class<? extends T> clazz) {
        T res;
        try {
            res = clazz.cast(internalData);
        } catch (Exception e) {
            res = null;
        }
        return res;
    }

    public <T> T getLocalData(String name, Class<? extends T> clazz) {
        return getLocalData(name, clazz, null);
    }

    public Object getLocalData(String name) {
        return getLocalData(name, Object.class);
    }

    public <T> T getLocalData(String name, Class<? extends T> clazz, T defaultValue) {
        T res;
        try {
            res = clazz.cast(localData.getOrDefault(name, defaultValue));
        } catch (Exception e) {
            res = defaultValue;
        }
        return res;
    }

    @SuppressWarnings("unused")
    public Object getLocalData(String name, Object value) {
        return getLocalData(name, Object.class, value);
    }

    public void setLocalData(String name, Object value) {
        localData.put(name, value);
    }

    public void incLocalData(String name, long delta) {
        Long value = getLocalData(name, Long.class);
        setLocalData(name, value == null ? delta : value + delta);
    }

    @SuppressWarnings("unused")
    public void incLocalData(String name) {
        incLocalData(name, 1);
    }

    @SuppressWarnings("unused")
    public void clearLocalData() {
        localData.clear();
    }

    public void addInput(String nameOfSet, String itemName, Class<?> inputClass) {
        Input res = new Input(this, nameOfSet, itemName, inputClass);
        inputs.add(res);
        List<Input> list = jointSet.computeIfAbsent(nameOfSet, key -> new ArrayList<>());
        res.index = list.size();
        list.add(res);
    }

    @SuppressWarnings("unused")
    public int getInputCount(String nameOfSet) {
        List<Input> list = getInputs(nameOfSet);
        return list == null ? 0 : list.size();
    }

    public Input getInput(String nameOfSet, int index) {
        List<Input> list = getInputs(nameOfSet);
        return index < 0 || list == null || index >= list.size() ? null : list.get(index);
    }

    public List<Input> getInputs(String nameOfSet) {
        return jointSet.getOrDefault(nameOfSet, null);
    }

    public <T> T getInputValue(String nameOfSet, int index, Class<? extends T> clazz) {
        Input input = getInput(nameOfSet, index);
        return input == null ? null : input.getValue(clazz);
    }

    @SuppressWarnings("unused")
    public Object getInputValue(String nameOfSet, int index) {
        return getInputValue(nameOfSet, index, Object.class);
    }

    public <T> T getPipelineParam(String name, Class<? extends T> clazz) {
        return pipeline.getParam(name, clazz);
    }

    @SuppressWarnings("unused")
    public Object getPipelineParam(String name) {
        return pipeline.getParam(name);
    }

    public void setPipelineParam(String name, Object value) {
        pipeline.setParam(name, value);
    }

    <T> T getValue(Class<? extends T> clazz) {
        return clazz.cast(outputValue);
    }

    void prepareInputs() {
        for (Input input : inputs) {
            if (!input.prepare()) {
                terminate();
            }
        }
    }

    boolean beforePipelineExecution() {
        if (terminated()) return true;
        prototype.beforePipelineExecution(this);
        return terminated();
    }

    boolean tryToExecute() {
        if (completed || visited || terminated()) return false;

        for (Input input : inputs) {
            if (!input.isCompleted()) return false;
        }
        completed = true;
        outputValue = prototype.execute(this);
        resetDependencies();
        if (!completed) {
            visited = true;
            return false;
        } else {
            if ((flags & FLAG_ITERATOR) != 0) {
                flags |= FLAG_EOF;
            }
        }
        return true;
    }

    public void yieldBegin() {
        if ((flags & FLAG_ITERATOR) == 0) {
            flags = FLAG_ITERATOR | FLAG_BOF;
        }
    }

    private void resetDependencies() {
        for (Item item : dependencies) {
            item.completed = false;
            item.visited = false;
        }
    }

    public void yieldResult(Object output, boolean error) {
        if ((flags & FLAG_ITERATOR) == 0) return;
        if (error) flags |= FLAG_ERROR;
        outputValue = output;
        resetDependencies();
        pipeline.doExecute(index + 1);
        flags &= ~(FLAG_ERROR | FLAG_BOF);
    }

    @SuppressWarnings("unused")
    public void setError() {
        flags |= FLAG_ERROR;
    }

    public void stayUncompleted() {
        completed = false;
    }

    @SuppressWarnings("unused")
    public void continueCompleted() {
        completed = true;
    }

    public void terminate() {
        pipeline.terminated = true;
    }

    @SuppressWarnings("unused")
    public boolean terminated() {
        return pipeline.terminated;
    }

    void doClose() {
        if (closed) return;
        try {
            prototype.close(this);
        } catch (Exception e) {
            log.warn("Pipeline item error on close {}", name);
        }
        closed = true;
    }

    private String doGetString(Object ... args) {
        return MessageFormatter.arrayFormat("(name: {}, prototype: {})", args).getMessage();
    }

    @Override
    public String toString() {
        return doGetString(name, prototype.getClass().getName());
    }
}
