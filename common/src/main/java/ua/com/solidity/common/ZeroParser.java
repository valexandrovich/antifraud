package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;

public class ZeroParser extends CustomParser {
    @Override
    protected boolean doOpen() {
        return true;
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
