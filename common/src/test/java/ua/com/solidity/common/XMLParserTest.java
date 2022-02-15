package ua.com.solidity.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataObject;
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
    private static final String XML_TEST_STRING = "<?xml version=\"1.1\" encoding=\"UTF-8\"?><data><group><item><name>Hello</name></item><item><name>World</name></item></group></data>";
    private static final String XML_PARAMS_STRING = ("{'path': ['data', 'group']}").replace("'", "\"");
    private static final String[] XML_TEST_RESULT = new String[]{"Hello", "World"};

    private static final String XML_TEST_STRING2 = "<?xml version=\"1.1\" encoding=\"UTF-8\"?><data><item><name>Иванов</name></item><item><name>Иван</name></item><item><name>Иванович</name></item></data>";
    private static final String XML_PARAMS_STRING2 = ("{'path': ['data']}").replace("'", "\"");
    private static final String[] XML_TEST_RESULT2 = new String[]{"Иванов", "Иван", "Иванович"};

    private List<DataObject> doTest(String paramsData, String XMLData) {
        List<DataObject> res = new ArrayList<>();
        XMLParams params = Utils.jsonToValue(paramsData, XMLParams.class);
        XMLParser parser = new XMLParser(params);
        try {
            InputStream stream = new ByteArrayInputStream(XMLData.getBytes(StandardCharsets.UTF_8));
            parser.open(stream);
            while (parser.hasData()) {
                res.add(parser.dataObject());
                parser.next();
            }
        } catch (Exception e) {
            log.error("XML open error.", e);
        }
        return res;
    }

    private boolean compare(List<DataObject> nodes, String[] testResult) {
        if (nodes.size() != testResult.length) return false;
        for (int i = 0; i < nodes.size(); ++i) {
            DataObject obj = nodes.get(i);
            DataField field = obj.getField("name");
            if (!(DataField.isString(field) && DataField.getString(field).equals(testResult[i]))) {
                return false;
            }
        }
        return true;
    }

    @Test
    void xmlTestFirst() {
        List<DataObject> res = doTest(XML_PARAMS_STRING, XML_TEST_STRING);
        assertThat(compare(res, XML_TEST_RESULT)).isTrue();
    }

    @Test
    void xmlTestSecond() {
        List<DataObject> res = doTest(XML_PARAMS_STRING2, XML_TEST_STRING);
        assertThat(compare(res, XML_TEST_RESULT2)).isFalse();
    }

    @Test
    void xmlTestThird() {
        List<DataObject> res = doTest(XML_PARAMS_STRING2, XML_TEST_STRING2);
        assertThat(compare(res, XML_TEST_RESULT2)).isTrue();
    }
}
