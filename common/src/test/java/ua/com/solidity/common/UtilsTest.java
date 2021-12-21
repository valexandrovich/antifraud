package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UtilsTest {
    private static final String testJson = ("{'lastName': 'Иванов', 'firstName': 'Иван', 'surName': 'Иванович'}").replace("'", "\"");
    @Test
    void JsonValueTestFirst() {
        JsonNode node = Utils.getJsonNode(testJson);
        String value = Utils.getNodeValue(node, "lastName", String.class);
        assertThat(value != null && value.equals("Иванов")).isTrue();
    }
}
