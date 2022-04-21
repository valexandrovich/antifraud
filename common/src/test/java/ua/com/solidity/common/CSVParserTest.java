package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import ua.com.solidity.common.data.DataField;
import ua.com.solidity.common.data.DataObject;
import ua.com.solidity.common.parsers.csv.CSVParams;
import ua.com.solidity.common.parsers.csv.CSVParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CSVParserTest {
    private static final String CSVTestFirst = "lastName, firstName,surName\n\"Иванов\",\"Иван\",\"Иванович\"\n\"Петров\",\"Петр\",\"Петрович\"";
    private static final String CSVTestSecond = "lastName, firstName,surName\n\"\nИванов\",\"Иван\n\",\"Иванович\"\n\"Петров\",\"Петр\",\"Петрович\"";
    private static final String CSVTestFifth = "name,id\n\"ТОВ \\\"Рога и копыта\\\"\", 1234567\n\"ЗАО \\\"Вася и Петя\\\"\", 7654321";
    private static final String[] firstTestResult = new String[] {
            ("{'lastName': 'Иванов', 'firstName': 'Иван', 'surName': 'Иванович'}").replace("'", "\""),
            ("{'lastName': 'Петров', 'firstName': 'Петр', 'surName': 'Петрович'}").replace("'", "\"")
    };

    private static final String[] fifthTestResult = new String[] {
            ("{'name': 'ТОВ \\'Рога и копыта\\'', 'id': '1234567'}").replace("'", "\""),
            ("{'name': 'ЗАО \\'Вася и Петя\\'', 'id': '7654321'}").replace("'", "\""),
    };


    private static final String CSVTestEx = "\"number\"\t\"date\"\t\"type\"\t\"firm_edrpou\"\t\"firm_name\"\t\"case_number\"\t\"start_date_auc\"\t\"end_date_auc\"\t\"court_name\"\t\"end_registration_date\"\n" +
            "\"68462\"\t\"16.02.2022\"\t\"Повідомлення про відкриття провадження у справі про банкрутство\"\t\"37945813\"\t\"Товариство з обмеженою відповідальністю \"\"Сервіс по над усе\"\" м.Маріуполь                                                                                                                                 \"\t\"905/103/22\"\t\t\t\"Господарський суд Донецької області\"\t\n" +
            "\"68463\"\t\"16.02.2022\"\t\"Повідомлення про відкриття провадження у справі про банкрутство\"\t\"42555302\"\t\"Товариство з обмеженою відповідальністю \"\"СТРОЙДОР\"\"                                                                                                                                                      \"\t\"922/193/22\"\t\t\t\"Господарський суд Харківської області\"\t\n" +
            "\"68461\"\t\"16.02.2022\"\t\"Повідомлення про відкриття провадження у справі про банкрутство\"\t\"42050897\"\t\"Товариство з обмеженою відповідальністю \"\"ТД АРІАНТ\"\" м.Маріуполь                                                                                                                                         \"\t\"905/104/22\"\t\t\t\"Господарський суд Донецької області\"\t\n" +
            "\"68449\"\t\"15.02.2022\"\t\"Повідомлення про відкриття провадження у справі про банкрутство\"\t\"05432916\"\t\"Комунальне виробниче житлове ремонтно-експлуатаційне підприємство Індустріального району                                                                                                                \"\t\"904/367/22\"\t\t\t\"Господарський суд Дніпропетровської області\"\t\n" +
            "\"68453\"\t\"15.02.2022\"\t\"Повідомлення про визнання боржника банкрутом і введення процедури погашення боргів боржника\"\t\"2434600584\"\t\"Грицакова Вікторія Миколаївна                                                                                                                                                                           \"\t\"904/7989/21\"\t\t\t\"Господарський суд Дніпропетровської області\"\t\n";

    private List<DataObject> getParsedList(CSVParams params, String data) {
        CSVParser parser = new CSVParser(params);
        InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        if (!parser.open(stream)) return null;
        List<DataObject> res = new ArrayList<>();
        while (parser.hasData()) {
            DataObject obj = parser.dataObject();
            if (obj != null) {
                res.add(obj);
            }
            parser.next();
        }
        return res;
    }

    private boolean doTestAt(CSVParams params, String data, String[] testRes) {
        List<DataObject> objects = getParsedList(params, data);
        if (objects == null || objects.size() != firstTestResult.length) return false;

        for (int i = 0; i < testRes.length; ++i) {
            DataObject obj = objects.get(i);
            if (obj == null) return false;
            JsonNode first = obj.getNode();
            JsonNode second = Utils.getJsonNode(testRes[i]);
            if (first == null || !first.equals(second)) return false;
        }
        return true;
    }

    public boolean doTestFirst() {
        CSVParams params = new CSVParams("UTF-8", ",", "\"", null,
                "\t ", CSVParams.FLAG_PARSE_FIELD_NAMES);
        return doTestAt(params, CSVTestFirst, firstTestResult);
    }

    public boolean doSecondTest() {
        CSVParams params = new CSVParams("UTF-8", ",", "\"", null,
                "\\t ",CSVParams.FLAG_SPLIT_MODE | CSVParams.FLAG_PARSE_FIELD_NAMES);
        return doTestAt(params, CSVTestFirst, firstTestResult);
    }

    public boolean doThirdTest() {
        CSVParams params = new CSVParams("UTF-8", ",", "\"", null,
                "\\t ", CSVParams.FLAG_PARSE_FIELD_NAMES | CSVParams.FLAG_AUTO_TRIM);
        return doTestAt(params, CSVTestSecond, firstTestResult);
    }

    public boolean doFourthTest() {
        CSVParams params = new CSVParams("UTF-8", "\t", "\"", null,
                "\b\r\f\t ", CSVParams.FLAG_PARSE_FIELD_NAMES | CSVParams.FLAG_AUTO_TRIM);
        List<DataObject> objects = getParsedList(params, CSVTestEx);
        if (objects == null) return false;

        for (DataObject obj : objects) {
            DataField field;
            String value;
            if (obj == null || (field = obj.getField("number")) == null ||
                    (value = DataField.getString(field)) == null || (value.indexOf('\"') >= 0)) return false;
        }
        return true;
    }

    public boolean doFifthTest() {
        CSVParams params = new CSVParams("UTF-8", ",", "\"", "\\\\",
                "\\t ", CSVParams.FLAG_PARSE_FIELD_NAMES | CSVParams.FLAG_AUTO_TRIM);
        return doTestAt(params, CSVTestFifth, fifthTestResult);
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

    @Test
    void fourthTest() { assertThat(doFourthTest()).isTrue(); }

    @Test
    void fifthTest() {
        assertThat(doFifthTest()).isTrue();
    }
}
