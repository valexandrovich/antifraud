package ua.com.solidity.common.parsers.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

@Getter
@Setter
@NoArgsConstructor
public class CSVParams {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_DELIMITER = ";";
    public static final String DEFAULT_QUOTE = "\"";
    public static final String DEFAULT_IGNORE_CHARS_NEAR_DELIMITER = "\b\r\f\t ";

    private boolean splitMode;
    private String encoding = DEFAULT_ENCODING;
    private boolean parseFieldNames;
    private String delimiter = DEFAULT_DELIMITER;
    private String quote = DEFAULT_QUOTE;
    private String ignoreCharsNearDelimiter = DEFAULT_IGNORE_CHARS_NEAR_DELIMITER;
    private boolean escapeUsing;

    public CSVParams(boolean splitMode, String encoding, boolean parseFieldNames, String delimiter, String quote, String ignoreCharsNearDelimiter, boolean escapeUsing) {
        this.splitMode = splitMode;
        setEncoding(encoding);
        this.parseFieldNames = parseFieldNames;
        setDelimiter(delimiter);
        setQuote(quote);
        setIgnoreCharsNearDelimiter(ignoreCharsNearDelimiter);
        this.escapeUsing = escapeUsing;
    }

    @JsonIgnore
    public final String lineSeparator = System.lineSeparator();

    private String nullOrEmpty(String value, String def) {
        return value == null || value.length() == 0 ? def : value;
    }

    @SuppressWarnings("unused")
    public final void setEncoding(String value) {
        this.encoding = nullOrEmpty(value, DEFAULT_ENCODING);
    }

    @SuppressWarnings("unused")
    public final void setDelimiter(String value) {
        this.delimiter = nullOrEmpty(StringEscapeUtils.unescapeJava(value), DEFAULT_DELIMITER);
    }

    public final char getDelimiterChar() {
        return nullOrEmpty(delimiter, DEFAULT_DELIMITER).charAt(0);
    }

    public final char getQuoteChar() {
        return nullOrEmpty(quote, DEFAULT_QUOTE).charAt(0);
    }

    @SuppressWarnings("unused")
    public final void setQuoteString(String value) {
        this.delimiter = nullOrEmpty(StringEscapeUtils.unescapeJava(value), DEFAULT_QUOTE);
    }

    @SuppressWarnings("unused")
    public final void setIgnoreCharsNearDelimiter(String value) {
        this.ignoreCharsNearDelimiter = nullOrEmpty(StringEscapeUtils.unescapeJava(value), DEFAULT_IGNORE_CHARS_NEAR_DELIMITER);
    }

    public final boolean isIgnoredChar(char value) {
        return ignoreCharsNearDelimiter.indexOf(value) >= 0;
    }
}
