package ua.com.solidity.common.pgsql;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class SQLFieldMapping {
    public static final String PATH = "path";
    public static final String ARGS = "args";
    public static final String BLANK_NAME = ".";
    public final String valuePath;
    public final List<String> mappingArgs = new ArrayList<>();

    SQLFieldMapping(String fieldName, JsonNode node) {
        if (node != null && !node.isNull()) {
            if (node.isTextual()) {
                valuePath = internalGetValuePath(node.asText(fieldName), fieldName);
                return;
            } else if (node.isObject()) {
                if (node.hasNonNull(PATH)) {
                    valuePath = internalGetValuePath(node.get(PATH).asText(fieldName), fieldName);
                } else {
                    valuePath = fieldName;
                }
                lookupArgs(node);
                return;
            }
        }
        valuePath = fieldName;
    }

    private String internalGetValuePath(String value, String fieldName) {
        return value == null || value.isBlank() || value.equals(BLANK_NAME) ? fieldName : value;
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
