package ua.com.solidity.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

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
                addJoint(nameOfSet, item, clazz);
            }
        }
    }

    public <T> T getLocalData(String name, Class<? extends T> clazz) {
        return getLocalDataDef(name, clazz, null);
    }

    public Object getLocalData(String name) {
        return getLocalData(name, Object.class);
    }

    public <T> T getLocalDataDef(String name, Class<? extends T> clazz, T defaultValue) {
        return clazz.cast(localData.getOrDefault(name, defaultValue));
    }

    @SuppressWarnings("unused")
    public Object getLocalDataDef(String name, Object value) {
        return getLocalDataDef(name, Object.class, value);
    }

    public void setLocalData(String name, Object value) {
        localData.put(name, value);
    }

    @SuppressWarnings("unused")
    public void clearLocalData() {
        localData.clear();
    }

    public void addJoint(String nameOfSet, String itemName, Class<?> inputClass) {
        Input res = new Input(this, nameOfSet, itemName, inputClass);
        inputs.add(res);
        List<Input> list = jointSet.computeIfAbsent(nameOfSet, key -> new ArrayList<>());
        res.index = list.size();
        list.add(res);
    }

    @SuppressWarnings("unused")
    public int getJointCount(String nameOfSet) {
        List<Input> list = getJoints(nameOfSet);
        return list == null ? 0 : list.size();
    }

    public Input getJoint(String nameOfSet, int index) {
        List<Input> list = getJoints(nameOfSet);
        return index < 0 || list == null || index >= list.size() ? null : list.get(index);
    }

    public List<Input> getJoints(String nameOfSet) {
        return jointSet.getOrDefault(nameOfSet, null);
    }

    public <T> T getJointValue(String nameOfSet, int index, Class<? extends T> clazz) {
        Input input = getJoint(nameOfSet, index);
        return input == null ? null : input.getValue(clazz);
    }

    @SuppressWarnings("unused")
    public Object getJointValue(String nameOfSet, int index) {
        return getJointValue(nameOfSet, index, Object.class);
    }

    @SuppressWarnings("unused")
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

    void prepareJoints() {
        for (Input input : inputs) {
            if (!input.prepare()) {
                terminate();
            }
        }
    }

    boolean tryToExecute() {
        if (completed || visited) return false;

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

    public void stayUncompleted() {
        completed = false;
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
}
