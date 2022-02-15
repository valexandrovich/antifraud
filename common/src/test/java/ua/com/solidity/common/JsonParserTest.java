package ua.com.solidity.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.parsers.json.JSONParams;
import ua.com.solidity.common.parsers.json.JSONParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class JsonParserTest {
    private static final String JSON_TEST_STRING = "[{'name':'Hello'}, {'name':'World'}]".replace("'", "\"");
    private static final String JSON_PARAMS_STRING = ("{}").replace("'", "\"");
    private static final String[] JSON_TEST_RESULT = new String[]{"Hello", "World"};

    private static final String JSON_TEST_STRING2 = "{'data': {'group':[{'name': 'Иванов'}, {'name': 'Иван'}, {'name': 'Иванович'}]}}".replace("'", "\"");
    private static final String JSON_PARAMS_STRING2 = ("{'encoding': 'windows-1251', 'path': ['data', 'group']}").replace("'", "\"");
    private static final String[] JSON_TEST_RESULT2 = new String[]{"Иванов", "Иван", "Иванович"};

    private List<DataObject> doTest(String paramsData, String XMLData) {
        List<DataObject> res = new ArrayList<>();
        JSONParams params = Utils.jsonToValue(paramsData, JSONParams.class);
        JSONParser parser = new JSONParser(params);
        try {
            InputStream stream = new ByteArrayInputStream(XMLData.getBytes(params.getCharset()));
            parser.open(stream);
            while (parser.hasData()) {
                res.add(parser.dataObject());
                parser.next();
            }
        } catch (Exception e) {
            log.error("JSON open error.", e);
        }
        return res;
    }

    private boolean compare(List<DataObject> nodes, String[] testResult) {
        if (nodes.size() != testResult.length) return false;
        for (int i = 0; i < nodes.size(); ++i) {
            DataField field = nodes.get(i).getField("name");
            if (!(DataField.isString(field) && DataField.getString(field).equals(testResult[i]))) {
                return false;
            }
        }
        return true;
    }

    @Test
    void jsonTestFirst() {
        List<DataObject> res = doTest(JSON_PARAMS_STRING, JSON_TEST_STRING);
        assertThat(compare(res, JSON_TEST_RESULT)).isTrue();
    }

    @Test
    void jsonTestSecond() {
        List<DataObject> res = doTest(JSON_PARAMS_STRING2, JSON_TEST_STRING);
        assertThat(compare(res, JSON_TEST_RESULT2)).isFalse();
    }

    @Test
    void jsonTestThird() {
        List<DataObject> res = doTest(JSON_PARAMS_STRING2, JSON_TEST_STRING2);
        assertThat(compare(res, JSON_TEST_RESULT2)).isTrue();
    }
}
