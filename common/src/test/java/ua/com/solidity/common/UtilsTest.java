package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UtilsTest {
    private static final String testJson = ("{'lastName': 'Иванов', 'firstName': 'Иван', 'surName': 'Иванович'}").replace("'", "\"");
    private static final String testJson2 = ("{'firstName': 'Иван', 'lastName': 'Иванов', 'surName': 'Иванович'}").replace("'", "\"");
    private static final byte[] testBytes = new byte[] {0x01, (byte)0xA0, 0x56, (byte)0xFF, (byte)0xFA};
    private static final String testBytesString = "1A056FFFA";
    private static final String testBytesResString = "0" + testBytesString;

    @Test
    void JsonValueTestFirst() {
        JsonNode node = Utils.getJsonNode(testJson);
        String value = Utils.getNodeValue(node, "lastName", String.class);
        assertThat(value != null && value.equals("Иванов")).isTrue();
    }

    @Test
    void HexTest() {
        assertThat(Utils.bytesToHex(testBytes)).isEqualTo(testBytesResString);
        assertThat(Utils.hexToBytes(testBytesString)).isEqualTo(testBytes);
    }

    @Test
    void DigestTest() {  // POC for node comparison
        byte[] firstDigest = Utils.complexDigest(Utils.getJsonNode(testJson));
        byte[] secondDigest = Utils.complexDigest(Utils.getJsonNode(testJson2));
        assertThat(firstDigest.length > 0).isTrue();
        assertThat(firstDigest).isEqualTo(secondDigest);
    }
}
