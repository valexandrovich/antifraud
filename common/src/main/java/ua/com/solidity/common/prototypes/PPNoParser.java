package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.ZeroParser;

public class PPNoParser extends PPCustomParser {
    @Override
    protected CustomParser createParser(JsonNode format) {
        return new ZeroParser();
    }
}
