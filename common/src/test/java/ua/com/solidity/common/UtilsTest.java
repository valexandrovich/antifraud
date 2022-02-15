package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
class UtilsTest {
    private static final String[] dateValues = {"31/5/2017 11:12:40.427"};
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

    private String getTestDate() {
        return "31/5/2017 11:12:40.427847";
    }

    @Test
    void dateParseTest() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy[[' ']['T'][H:mm[:ss[.n][XXX]");
        boolean res = true;
        try {
            for (String dateValue : dateValues) {
                LocalDateTime.parse(dateValue, fmt);
            }
        } catch(Exception e) {
            res = false;
        }

        assertThat(res).isTrue();
    }

    @Test
    void dateTimeParseTest() {
        final String value = getTestDate();
        DurationPrinter printer = new DurationPrinter();
        log.info("before execution.");
        for (int i = 0; i < 16484; ++i) {
            ValueParser.getLocalDateTime(value);
        }
        printer.stop();
        log.info("elapsed time: {}", printer.getDurationString());
        assertThat(printer.getDurationMillis()).isLessThan(1000);
    }

    @Test
    void formatterTest() {
        assertThat(Utils.messageFormat("{}:{}", "hello", "world")).isEqualTo("hello:world");
    }
}
