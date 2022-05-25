package ua.com.solidity.common;
import lombok.CustomLog;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@CustomLog
class FilteredTextInputStreamTest {
    public static final byte[] data = {49, 10, 50, 13, 10, 51, 13, 52, 10, 13, 53, 10, 54, 10, 55, 13, 56};
    @Test
    void ReadTest() {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data); FilteredTextInputStream filtered = new FilteredTextInputStream(stream, 1024)) {
            Scanner scanner = new Scanner(new InputStreamReader(filtered, StandardCharsets.UTF_8));
            while (scanner.hasNextLine()) {
                String s = scanner.nextLine().trim();
                log.info("String: ({})", s);
            }
            assertThat(true).isTrue();
        } catch (Exception e) {
            assertThat(false).isTrue();
        }
    }
}
