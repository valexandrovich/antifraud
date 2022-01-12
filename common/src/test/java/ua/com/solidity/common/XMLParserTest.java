package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ua.com.solidity.common.parsers.xml.XMLParams;
import ua.com.solidity.common.parsers.xml.XMLParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class XMLParserTest {
    private static final String XML_TEST_STRING = "<?xml version=\"1.1\" encoding=\"UTF-8\"?><data><group><item>Hello</item><item>World</item></group></data>";
    private static final String XML_PARAMS_STRING = ("{'path': ['data', 'group']}").replace("'", "\"");
    private static final String[] XML_TEST_RESULT = new String[]{"Hello", "World"};

    private static final String XML_TEST_STRING2 = "<?xml version=\"1.1\" encoding=\"UTF-8\"?><data><item>Иванов</item><item>Иван</item><item>Иванович</item></data>";
    private static final String XML_PARAMS_STRING2 = ("{'path': ['data']}").replace("'", "\"");
    private static final String[] XML_TEST_RESULT2 = new String[]{"Иванов", "Иван", "Иванович"};

    private List<JsonNode> doTest(String paramsData, String XMLData) {
        List<JsonNode> res = new ArrayList<>();
        XMLParams params = Utils.jsonToValue(paramsData, XMLParams.class);
        XMLParser parser = new XMLParser(params);
        try {
            InputStream stream = new ByteArrayInputStream(XMLData.getBytes(StandardCharsets.UTF_8));
            parser.open(stream);
            while (parser.hasData()) {
                res.add(parser.getNode());
                parser.next();
            }
        } catch (Exception e) {
            log.error("XML open error.", e);
        }
        return res;
    }

    private boolean compare(List<JsonNode> nodes, String[] testResult) {
        if (nodes.size() != testResult.length) return false;
        for (int i = 0; i < nodes.size(); ++i) {
            JsonNode node = nodes.get(i);
            if (!(node.isTextual() && node.asText().equals(testResult[i]))) {
                return false;
            }
        }
        return true;
    }

    @Test
    void xmlTestFirst() {
        List<JsonNode> res = doTest(XML_PARAMS_STRING, XML_TEST_STRING);
        assertThat(compare(res, XML_TEST_RESULT)).isTrue();
    }

    @Test
    void xmlTestSecond() {
        List<JsonNode> res = doTest(XML_PARAMS_STRING2, XML_TEST_STRING);
        assertThat(compare(res, XML_TEST_RESULT2)).isFalse();
    }

    @Test
    void xmlTestThird() {
        List<JsonNode> res = doTest(XML_PARAMS_STRING2, XML_TEST_STRING2);
        assertThat(compare(res, XML_TEST_RESULT2)).isTrue();
    }
}
