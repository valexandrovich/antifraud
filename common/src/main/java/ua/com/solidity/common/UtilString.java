package ua.com.solidity.common;

import java.time.DateTimeException;
import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;

public final class UtilString {

    private UtilString(){}

    public static String toUpperCase(String value) {
        return value == null ? null : value.toUpperCase();
    }

    public static String toLowerCase(String value) {
        return value == null ? null : value.toLowerCase();
    }

    public static boolean equalsIgnoreCase(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }

    public static boolean matches(String value, String regex) {
        return value != null && regex != null && value.matches(regex);
    }

    public static LocalDate stringToDate(String date) {
        LocalDate localDate = null;
        try {
            if (StringUtils.isNotBlank(date)) {
                String[] split = date.split("[.-]");
                if (split.length == 3) {
                    split[0] = split[0].length() == 1 ? "0" + split[0] : split[0];
                    split[1] = split[1].length() == 1 ? "0" + split[1] : split[1];
                    String dateCorrected = split[0] + "." + split[1] + "." + split[2];

                    localDate = LocalDate.of(Integer.parseInt(dateCorrected.substring(6)),
                                             Integer.parseInt(dateCorrected.substring(3, 5)),
                                             Integer.parseInt(dateCorrected.substring(0, 2)));
                }
            }
        } catch (DateTimeException e) {
            return localDate;
        }
        return localDate;
    }
}
