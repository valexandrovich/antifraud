package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.parsers.csv.CSVParams;
import ua.com.solidity.common.parsers.csv.CSVParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CSVParserTest {
    private static final String CSVTestFirst = "lastName, firstName,surName\n\"Иванов\",\"Иван\",\"Иванович\"\n\"Петров\",\"Петр\",\"Петрович\"";
    private static final String CSVTestSecond = "lastName, firstName,surName\n\"\nИванов\",\"Иван\n\",\"Иванович\"\n\"Петров\",\"Петр\",\"Петрович\"";
    private static final String[] firstTestResult = new String[] {
            ("{'lastName': 'Иванов', 'firstName': 'Иван', 'surName': 'Иванович'}").replace("'", "\""),
            ("{'lastName': 'Петров', 'firstName': 'Петр', 'surName': 'Петрович'}").replace("'", "\"")
    };

    private boolean doTestAt(CSVParams params, String data) {
        CSVParser parser = new CSVParser(params);
        InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        if (!parser.open(stream)) return false;
        int i = 0;

        while (parser.hasData()) {
            DataObject obj = parser.dataObject();
            JsonNode first = obj == null ? null : obj.getNode();
            if (i < firstTestResult.length) {
                JsonNode second = Utils.getJsonNode(firstTestResult[i++]);
                if (first == null || !first.equals(second)) return false;
            } else return false;
            parser.next();
        }
        return true;
    }

    public boolean doTestFirst() {
        CSVParams params = new CSVParams("UTF-8", ",", "\\\"",
                "\\t ", CSVParams.FLAG_PARSE_FIELD_NAMES);
        return doTestAt(params, CSVTestFirst);
    }

    public boolean doSecondTest() {
        CSVParams params = new CSVParams("UTF-8", ",", "\\\"",
                "\\t ",CSVParams.FLAG_SPLIT_MODE | CSVParams.FLAG_PARSE_FIELD_NAMES);
        return doTestAt(params, CSVTestFirst);
    }

    public boolean doThirdTest() {
        CSVParams params = new CSVParams("UTF-8", ",", "\\\"",
                "\\t ", CSVParams.FLAG_PARSE_FIELD_NAMES | CSVParams.FLAG_AUTO_TRIM);
        return doTestAt(params, CSVTestSecond);
    }

    @Test
    void firstTest() {
        assertThat(doTestFirst()).isTrue();
    }

    @Test
    void secondTest() {
        assertThat(doSecondTest()).isTrue();
    }

    @Test
    void thirdTest() {
        assertThat(doThirdTest()).isTrue();
    }
}
