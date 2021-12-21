package ua.com.solidity.common.parsers.xls;

import com.fasterxml.jackson.databind.JsonNode;
import ua.com.solidity.common.CustomParser;

public class XLSParser extends CustomParser {
    @Override
    protected boolean doOpen() {
        return false;
    }

    @Override
    public JsonNode getNode() {
        return null;
    }

    @Override
    protected boolean doNext() {
        return false;
    }
}
