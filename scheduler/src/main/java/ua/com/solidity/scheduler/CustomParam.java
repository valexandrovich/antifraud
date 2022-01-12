package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;

public interface CustomParam {
    boolean isIgnored();
    JsonNode getNode();
    void setNode(JsonNode node);
}
