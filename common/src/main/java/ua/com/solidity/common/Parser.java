package ua.com.solidity.common;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Parser {
    ObjectNode node();
    boolean eof();
    void next();
}
