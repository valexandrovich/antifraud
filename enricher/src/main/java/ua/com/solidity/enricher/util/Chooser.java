package ua.com.solidity.enricher.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Objects;

public final class Chooser {
	private Chooser() {}

	public static <T> T chooseNotNull(T first, T second) {
		return second != null ? second : first;
	}

	public static LocalDate chooseNotNullOrDateNow(LocalDate first, LocalDate second) {
		LocalDate localDate = chooseNotNull(first, second);
		if (Objects.isNull(localDate)){
			return LocalDate.now();
		}
		return localDate;
	}

	public static String chooseNotBlank(String first, String second) {
		return !StringUtils.isBlank(second) ? second : first;
	}
}
