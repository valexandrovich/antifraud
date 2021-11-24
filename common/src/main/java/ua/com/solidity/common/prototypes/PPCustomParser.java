package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.Utils;
import ua.com.solidity.pipeline.Item;
import ua.com.solidity.pipeline.Prototype;

import java.io.InputStream;

public abstract class PPCustomParser extends Prototype {
    protected static final String STREAM = "stream";
    protected static final String PARSER = "parser";
    @Override
    public Class<?> getOutputClass() {
        return JsonNode.class;
    }

    @Override
    protected void initialize(Item item, JsonNode node) {
        item.mapInputs(STREAM, InputStream.class);
        item.setLocalData(PARSER, createParser(node));
    }

    protected abstract CustomParser createParser(JsonNode format);

    @Override
    protected Object execute(@NonNull Item item) {
        CustomParser parser = item.getLocalData(PARSER, CustomParser.class);
        InputStream stream = item.getInputValue(STREAM, 0, InputStream.class);
        boolean errorReporting;
        item.yieldBegin();
        if (parser.open(stream)) {
            while (parser.hasData()) {
                errorReporting = false;
                JsonNode node = parser.getNode();
                if (parser.isErrorReporting()) {
                    errorReporting = true;
                    node = Utils.getJsonNode(parser.getErrorReport());
                }
                item.yieldResult(node, errorReporting);
                parser.next();
            }
        }

        return null;
    }

    @Override
    protected void close(Item item) {
        item.getLocalData(PARSER, CustomParser.class).close();
    }
}
