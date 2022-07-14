package ua.com.solidity.enricher.util;

public class Regex {
    public final static String CONTAINS_NUMERAL_REGEX = "[0-9]+";
    public final static String ALL_NOT_NUMBER_REGEX = "[^0-9]";
    public final static String ALL_NUMBER_REGEX = "^[\\d]";
    public final static String INN_REGEX = "^[\\d]{10}$";
    public final static String OKPO_REGEX = "^[\\d]{8,9}$";
    public final static String PASS_NUMBER_REGEX = "^[\\d]{6}$";
    public final static String IDCARD_NUMBER_REGEX = "^[\\d]{9}$";
    public final static String FOREIGN_SERIES_REGEX = "^[A-Z]{2}$";
    public final static String DOMESTIC_SERIES_REGEX = "^[А-ГҐДЕЄЭЖЗИІЇЙ-ЩЬЮЯ]{2}$";
    public final static String RECORD_NUMBER_REGEX = "^[\\d]{8}-[\\d]{5}$";
    public final static String INN_FORMAT_REGEX = "%010d";
    public final static String EDRPOU_FORMAT_REGEX = "%08d";
    public final static String PDV_REGEX = "^[\\d]{12}$";
}