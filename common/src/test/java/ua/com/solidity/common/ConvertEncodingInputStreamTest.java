package ua.com.solidity.common;

import lombok.CustomLog;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;


@CustomLog
class ConvertEncodingInputStreamTest {
    private static final String firstTestData = "Hello, ребята, это простой тест";
    private static final String defCharsetName = "windows-1251";

    private String encodingTest(String charsetName) {
        String str;
        try {
            InputStream inputStream = new ByteArrayInputStream(firstTestData.getBytes(charsetName));
            ConvertEncodingInputStream testStream = new ConvertEncodingInputStream(inputStream, Charset.availableCharsets().get(charsetName), StandardCharsets.UTF_8);
            InputStreamReader reader = new InputStreamReader(testStream, StandardCharsets.UTF_8);
            Scanner scanner = new Scanner(reader);
            str = scanner.nextLine();
        } catch (Exception e) {
            log.error("Reading error.", e);
            str = null;
        }

        return str;
    }

    @Test
    void ConvertEncodingTestFirst() {
        assertThat(encodingTest(defCharsetName)).isEqualTo(firstTestData);
    }

    @Test
    void ConvertEncodingTestSecond() {
        assertThat(encodingTest("UTF-8")).isEqualTo(firstTestData);
    }
}
