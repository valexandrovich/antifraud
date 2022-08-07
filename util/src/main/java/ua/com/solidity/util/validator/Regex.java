package ua.com.solidity.util.validator;

public class Regex {
    public static final String INN_REGEX = "^[\\d]{10}$";
    public static final String OKPO_REGEX = "^[\\d]{8,9}$";
    public static final String INN_FORMAT_REGEX = "%010d";
    public static final String EDRPOU_FORMAT_REGEX = "%08d";
    public static final String PDV_REGEX = "^[\\d]{12}$";
}