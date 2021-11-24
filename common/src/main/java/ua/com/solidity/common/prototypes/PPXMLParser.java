package ua.com.solidity.common.prototypes;

import com.fasterxml.jackson.databind.JsonNode;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.parsers.xml.XMLParams;
import ua.com.solidity.common.parsers.xml.XMLParser;

public class PPXMLParser extends PPCustomParser {
    @Override
    protected XMLParser createParser(JsonNode format) {
        return new XMLParser(Utils.jsonToValue(format, XMLParams.class));
    }
}
