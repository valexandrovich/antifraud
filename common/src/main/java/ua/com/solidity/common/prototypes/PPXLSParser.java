package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.parsers.xls.XLSParams;
import ua.com.solidity.common.parsers.xls.XLSParser;

public class PPXLSParser extends PPCustomParser {
    @Override
    protected CustomParser createParser(JsonNode format) {
        return new XLSParser(Utils.jsonToValue(format, XLSParams.class));
    }
}
