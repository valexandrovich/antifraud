package ua.com.solidity.enricher.util;

public class Regex {
    public static final String CONTAINS_NUMERAL_REGEX = "[0-9]+";
    public static final String ALL_NOT_NUMBER_REGEX = "[^0-9]";
    public static final String ALL_NUMBER_REGEX = "^[\\d]+$";
    public static final String INN_REGEX = "^[\\d]{10}$";
    public static final String OKPO_REGEX = "^[\\d]{8,9}$";
    public static final String PASS_NUMBER_REGEX = "^[\\d]{6}$";
    public static final String IDCARD_NUMBER_REGEX = "^[\\d]{9}$";
    public static final String FOREIGN_SERIES_REGEX = "^[A-Z]{2}$";
    public static final String DOMESTIC_SERIES_REGEX = "^[А-ГҐДЕЄЭЖЗИІЇЙ-ЩЬЮЯ]{2}$";
    public static final String RECORD_NUMBER_REGEX = "^[\\d]{8}-[\\d]{5}$";
    public static final String INN_FORMAT_REGEX = "%010d";
    public static final String EDRPOU_FORMAT_REGEX = "%08d";
    public static final String PDV_REGEX = "^[\\d]{12}$";
}