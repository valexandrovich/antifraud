package ua.com.solidity.enricher.util;

import org.apache.commons.lang3.StringUtils;
import static ua.com.solidity.enricher.util.StringStorage.CYRILLIC_LETTERS;
import static ua.com.solidity.enricher.util.StringStorage.LATIN_LETTERS;

import java.time.LocalDate;

public class StringFormatUtil {

	public static String transliterationToCyrillicLetters(String serial) {
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

	public static LocalDate stringToDate(String date) {
		LocalDate localDate = null;
		if (!StringUtils.isBlank(date)) {
			localDate = LocalDate.of(Integer.parseInt(date.substring(6)),
			                         Integer.parseInt(date.substring(3, 5)),
			                         Integer.parseInt(date.substring(0, 2)));
		}
		return localDate;
	}
}
