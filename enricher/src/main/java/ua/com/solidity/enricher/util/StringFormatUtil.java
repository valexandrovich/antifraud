package ua.com.solidity.enricher.util;

import static ua.com.solidity.enricher.util.StringStorage.CYRILLIC_LETTERS;
import static ua.com.solidity.enricher.util.StringStorage.LATIN_LETTERS;

import org.apache.commons.lang3.StringUtils;

public class StringFormatUtil {
    private StringFormatUtil() {
    }

    public static String transliterationToCyrillicLetters(String serial) {
        if (StringUtils.isBlank(serial)) return null;
        StringBuilder cyrillicSerial = new StringBuilder();
        if (!StringUtils.isBlank(serial))
            for (int i = 0; i < serial.length(); i++) {
                int index = LATIN_LETTERS.indexOf(serial.charAt(i));
                if (index > -1) cyrillicSerial.append(CYRILLIC_LETTERS.charAt(index));
                else cyrillicSerial.append(serial.charAt(i));
            }
        return cyrillicSerial.toString();
    }

    public static String transliterationToLatinLetters(String serial) {
        if (StringUtils.isBlank(serial)) return null;
        StringBuilder latinSerial = new StringBuilder();
        for (int i = 0; i < serial.length(); i++) {
            int index = CYRILLIC_LETTERS.indexOf(serial.charAt(i));
            if (index > -1) latinSerial.append(LATIN_LETTERS.charAt(index));
            else latinSerial.append(serial.charAt(i));
        }
        return latinSerial.toString();
    }

    public static String importedRecords(long num) {
        return String.format("Imported %d records", num);
    }
}
