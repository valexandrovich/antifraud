package ua.com.solidity.common.data;

import com.fasterxml.jackson.databind.JsonNode;

public interface DataModifier {
    void handle(JsonNode node);
}
