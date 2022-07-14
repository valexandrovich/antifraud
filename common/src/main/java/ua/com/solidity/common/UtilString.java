package ua.com.solidity.common;

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
        return value != null && value.matches(regex);
    }
}
