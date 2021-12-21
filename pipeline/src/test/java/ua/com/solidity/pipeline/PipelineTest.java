package ua.com.solidity.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineTest {
    private static final String LINK = "link";
    private static final String ARRAY = "array";

    private static class ArrayPrototype extends Prototype {
        @Override
        public Class<?> getOutputClass() {
            return ArrayNode.class;
        }

        @Override
        protected void initialize(Item item, JsonNode node) {
            item.setLocalData(ARRAY, node != null && node.isArray() ? node : null);
        }

        @Override
        protected Object execute(@NonNull Item item) {
            return item.getLocalData(ARRAY);
        }

        @Override
        protected void close(Item item) {
            //nothing yet
        }
    }

    private static class EnumeratorPrototype extends Prototype {
        @Override
        public Class<?> getOutputClass() {
            return String.class;
        }

        @Override
        protected void initialize(Item item, JsonNode node) {
            item.mapInputs(LINK, ArrayNode.class);
        }

        @Override
        protected Object execute(@NonNull Item item) {
            item.yieldBegin();
            ArrayNode node = item.getInputValue(LINK, 0, ArrayNode.class);
            if (node != null) {
               for (int i = 0; i < node.size(); ++i) {
                    item.yieldResult(node.get(i).asText(), false);
                }
            }
            return null;
        }

        @Override
        protected void close(Item item) {
            //nothing yet
        }
    }

    @Slf4j
    private static class LoggerPrototype extends Prototype {
        @Override
        public Class<?> getOutputClass() {
            return null;
        }

        @Override
        protected void initialize(Item item, JsonNode node) {
            item.mapInputs(LINK, String.class);
        }

        @Override
        protected Object execute(@NonNull Item item) {
            String value = item.getInputValue(LINK, 0, String.class);
            if (value != null) {
                log.info("item found: {}", value);
            }
            return null;
        }

        @Override
        protected void close(Item item) {
            //nothing yet
        }
    }

    private static class CollectorPrototype extends Prototype {
        @Override
        public Class<?> getOutputClass() {
            return ArrayNode.class;
        }

        @Override
        protected void initialize(Item item, JsonNode node) {
            item.mapInputs(LINK, String.class);
            item.setLocalData("items", JsonNodeFactory.instance.arrayNode());
        }

        @Override
        protected Object execute(@NonNull Item item) {
            Input input = item.getInput(LINK, 0);
            ArrayNode node = item.getLocalData("items", ArrayNode.class);
            if (input != null && input.isIterator() && node != null) {
                if (input.isBOF()) node.removeAll();
                if (input.hasData()) {
                    node.add(input.getValue(String.class));
                    item.stayUncompleted();
                    return null;
                }
            }
            return node;
        }

        @Override
        protected void close(Item item) {
            //nothing yet
        }
    }

    private static class ComparerPrototype extends Prototype {
        @Override
        public Class<?> getOutputClass() {
            return null;
        }

        @Override
        protected void initialize(Item item, JsonNode node) {
            item.mapInputs(LINK, ArrayNode.class);
        }

        @Override
        protected Object execute(@NonNull Item item) {
            ArrayNode source = item.getInputValue(LINK, 0, ArrayNode.class);
            ArrayNode target = item.getInputValue(LINK, 1, ArrayNode.class);
            if (source != null && target != null) {
                boolean different;

                if (source.size() == target.size()) {
                    different = false;
                    for (int i = 0; i < source.size(); ++i) {
                        if (!source.get(i).asText().equals(target.get(i).asText())) {
                            different = true;
                            break;
                        }
                    }
                } else different = true;
                item.setPipelineParam("same", !different);
            }
            return null;
        }

        @Override
        protected void close(Item item) {
            //nothing yet
        }
    }

    SimplePipelinePrototypeProvider provider = new SimplePipelinePrototypeProvider().
            add("array", new ArrayPrototype()).
            add("enumerator", new EnumeratorPrototype()).
            add("logger", new LoggerPrototype()).
            add("collector", new CollectorPrototype()).
            add("comparer", new ComparerPrototype());

    PipelineFactory factory = new PipelineFactory(provider);
    String jsonString = ("{'pipeline': [" +
            "{'prototype' : 'enumerator', 'name' : 'enumSource', 'inputs': {'link': 'arraySource'}}," +
            "{'prototype': 'array', 'name': 'arraySource', 'data': ['Hello world', 'Hello world, again']}," +
            "{'prototype' : 'comparer', 'name' : 'comparer', 'inputs': {'link': ['arraySource', 'collector']}}," +
            "{'prototype' : 'logger', 'name' : 'logger', 'inputs': {'link': 'enumSource'}}," +
            "{'prototype' : 'collector', 'name' : 'collector', 'inputs': {'link': 'enumSource'}}" +
        "]}").replace("'", "\"");

    @Test
    void firstTest() {
        Pipeline pipeline = factory.createPipeline(jsonString);
        assertThat(pipeline.isValid()).isTrue();
        assertThat(pipeline.execute()).isTrue();
        assertThat(pipeline.getParam("same", Boolean.class)).isTrue();
    }
}
