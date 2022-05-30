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

    public static final int FLAG_SPLIT_MODE = 1;
    public static final int FLAG_PARSE_FIELD_NAMES = 2;
    public static final int FLAG_UNESCAPE_VALUES = 4;
    public static final int FLAG_AUTO_TRIM = 8;
    public static final int FLAG_AUTO_COMPLETE = 16;

    private boolean splitMode;
    private String encoding = DEFAULT_ENCODING;
    private boolean parseFieldNames;
    private String delimiter = DEFAULT_DELIMITER;
    private String quote = DEFAULT_QUOTE;
    private String escape = null;
    private String ignoreCharsNearDelimiter = DEFAULT_IGNORE_CHARS_NEAR_DELIMITER;
    private boolean unescapeValues = false;
    private boolean autoComplete = false;
    private boolean autoTrim = true;
    private int columnCount = 0;

    public CSVParams(String encoding, String delimiter, String quote, String escape, String ignoreCharsNearDelimiter, int flags, int columnCount) {
        this.columnCount = columnCount;
        this.splitMode = (flags & FLAG_SPLIT_MODE) != 0;
        setEncoding(encoding);
        this.parseFieldNames = (flags & FLAG_PARSE_FIELD_NAMES) != 0;
        setDelimiter(delimiter);
        setQuoteValues(quote, escape);
        setIgnoreCharsNearDelimiter(ignoreCharsNearDelimiter);
        this.unescapeValues = (flags & FLAG_UNESCAPE_VALUES) != 0;
        this.autoTrim = (flags & FLAG_AUTO_TRIM) != 0;
        this.autoComplete = (flags & FLAG_AUTO_COMPLETE) != 0;
    }

    public static int flags(boolean splitMode, boolean parseFieldNames, boolean unescapeValues, boolean autoTrim, boolean autoComplete) {
        int res = 0;
        if (splitMode) res |= FLAG_SPLIT_MODE;
        if (parseFieldNames) res |= FLAG_PARSE_FIELD_NAMES;
        if (unescapeValues) res |= FLAG_UNESCAPE_VALUES;
        if (autoTrim) res |= FLAG_AUTO_TRIM;
        if (autoComplete) res |= FLAG_AUTO_COMPLETE;
        return res;
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

    @JsonIgnore
    public final char getEscapeChar() {
        return escape == null ? quote.charAt(0) : escape.charAt(0);
    }

    @JsonIgnore
    public final boolean isEscapeCharNeeded() {
        return unescapeValues && getEscapeChar() == '\\';
    }

    public final void setQuote(String value) {
        setQuoteValues(value, escape);
    }

    public final void setEscape(String value) {
        setQuoteValues(quote, value);
    }

    public final void setQuoteValues(String quote, String escape) {
        this.quote = nullOrEmpty(StringEscapeUtils.unescapeJava(quote), DEFAULT_QUOTE).substring(0, 1);
        this.escape = escape == null || escape.isBlank() ? this.quote :
                nullOrEmpty(StringEscapeUtils.unescapeJava(escape), this.quote).substring(0, 1);
    }

    @JsonIgnore
    public final char getQuoteChar() {
        return quote.charAt(0);
    }

    @JsonIgnore
    public final boolean doubleQuoteMode() {
        return quote.equals(escape);
    }

    public final String recoverValue(String value) {
        if (!isEscapeCharNeeded()) {
            int searchStart = 0;
            int copyStart = 0;
            int pos;
            StringBuilder builder = new StringBuilder();
            while ((pos = value.indexOf(getEscapeChar(), searchStart)) >= 0) {
                if (pos > copyStart) {
                    builder.append(value, copyStart, pos);
                }
                copyStart = pos + 1;
                searchStart = copyStart + 1;
            }

            if (copyStart < value.length()) {
                builder.append(value, copyStart, value.length());
            }

            value = builder.toString();
        }
        return unescapeValues ? StringEscapeUtils.unescapeJava(value) : value;
    }

    @SuppressWarnings("unused")
    public final void setIgnoreCharsNearDelimiter(String value) {
        this.ignoreCharsNearDelimiter = nullOrEmpty(StringEscapeUtils.unescapeJava(value), DEFAULT_IGNORE_CHARS_NEAR_DELIMITER);
    }

    public final char getDelimiter() {
        return delimiter.charAt(0);
    }

    public final String getDelimiterRegex() {
        return "\\" + getDelimiter();
    }

    public final boolean isDelimiter(char value) {
        return delimiter.indexOf(value) >= 0;
    }

    public final boolean isIgnoredChar(char value) {
        return ignoreCharsNearDelimiter.indexOf(value) >= 0;
    }
}
