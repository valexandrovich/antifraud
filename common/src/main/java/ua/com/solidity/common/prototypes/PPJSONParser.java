package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.parsers.json.JSONParams;
import ua.com.solidity.common.parsers.json.JSONParser;

public class PPJSONParser extends PPCustomParser {
    @Override
    protected CustomParser createParser(JsonNode format) {
        return new JSONParser(Utils.jsonToValue(format, JSONParams.class));
    }
}
