package ua.com.solidity.importer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ua.com.solidity.common.Parser;

public class XMLParser implements Parser {

    @Override
    public ObjectNode node() {
        return null;
    }

    @Override
    public boolean eof() {
        return false;
    }

    @Override
    public void next() {

    }
}
