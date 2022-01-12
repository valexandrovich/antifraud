package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.parsers.csv.CSVParams;
import ua.com.solidity.common.parsers.csv.CSVParser;

public class PPCSVParser extends PPCustomParser {
    @Override
    protected CustomParser createParser(JsonNode format) {
        return new CSVParser(Utils.jsonToValue(format, CSVParams.class));
    }
}
