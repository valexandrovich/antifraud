package ua.com.solidity.report.utils;

import java.security.SecureRandom;

import static java.lang.Math.min;

public class Utils {
    private Utils() {}

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String pathFromName(byte[] bytes) {
        StringBuilder path = new StringBuilder();
        for (int i=0; i < min(bytes.length, 3); i++) {
            path.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1)).append("/");
        }
        return path.toString();
    }

    public static String[] randomName() {
        String[] result = new String[2];
        byte[] values = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(values);
        result[0] = pathFromName(values);
        result[1] = bytesToHex(values);
        return result;
    }

    public static String randomPath() {
        String[] path = randomName();
        return path[0] + path[1];
    }
}
