package ua.com.solidity.common;

import org.apache.commons.lang3.StringUtils;

public class Chooser {

	public static <T> T chooseSecondNotNull(T first, T second) {
		return second != null ? second : first;
	}

	public static String chooseNotBlank(String first, String second) {
		return !StringUtils.isBlank(second) ? second : first;
	}
}
