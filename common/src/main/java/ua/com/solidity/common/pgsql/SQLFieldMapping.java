package ua.com.solidity.common.pgsql;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class SQLFieldMapping {
    public static final String PATH = "path";
    public static final String ARGS = "args";
    public final String valuePath;
    public final List<String> mappingArgs = new ArrayList<>();

    SQLFieldMapping(String fieldName, JsonNode node) {
        if (node != null) {
            if (node.isTextual()) {
                valuePath = node.asText();
                return;
            } else if (node.isObject()) {
                if (node.hasNonNull(PATH)) {
                    valuePath = node.get(PATH).asText(fieldName);
                } else {
                    valuePath = fieldName;
                }
                lookupArgs(node);
                return;
            }
        }
        valuePath = fieldName;
    }

    private void lookupArgs(JsonNode node) {
        if (node.hasNonNull(ARGS)) {
            JsonNode nodeArgs = node.get(ARGS).isArray() ? node.get(ARGS) : null;
            if (nodeArgs != null) {
                for (int i = 0; i < nodeArgs.size(); ++i) {
                    String argText = nodeArgs.get(i).asText(null);
                    if (argText != null) {
                        mappingArgs.add(argText);
                    }
                }
            }
        }
    }
}
