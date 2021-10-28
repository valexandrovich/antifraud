package ua.com.solidity.importer;

public class CSVParams {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_DELIMITER = ";";
    public static final char DEFAULT_QUOTE = '\"';
    public static final String DEFAULT_IGNORE_CHARS_NEAR_DELIMITER = "\b\r\f\t ";

    public final boolean splitMode;
    public final String encoding;
    public final boolean parseFieldNames;
    public final String delimiter;
    public final char quote;
    public final String ignoreCharsNearDelimiter;

    public final String lineSeparator;

    public CSVParams(String encoding, boolean splitMode, boolean parseFieldNames, String delimiter, String quote, String ignoreCharsNearDelimiter) {
        this.lineSeparator = System.lineSeparator();
        this.splitMode = splitMode;
        this.parseFieldNames = parseFieldNames;
        this.encoding = encoding == null || encoding.length() == 0 ? DEFAULT_ENCODING : encoding;
        this.delimiter = delimiter == null || delimiter.length() == 0 ? DEFAULT_DELIMITER : delimiter;
        this.quote = quote == null || quote.length() != 1 ? DEFAULT_QUOTE : quote.charAt(0);
        this.ignoreCharsNearDelimiter = ignoreCharsNearDelimiter == null || ignoreCharsNearDelimiter.length() == 0 ?
                DEFAULT_IGNORE_CHARS_NEAR_DELIMITER : ignoreCharsNearDelimiter;
    }
}
