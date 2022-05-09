package ua.com.solidity.web.utils;

public final class UtilString {

    private UtilString(){}

    public static String toUpperCase(String value) {
        return value == null ? null : value.toUpperCase();
    }

    public static String toLowerCase(String value) {
        return value == null ? null : value.toLowerCase();
    }
}
