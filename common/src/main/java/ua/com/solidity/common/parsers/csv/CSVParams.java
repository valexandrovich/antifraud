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
    public static final int FLAG_ESCAPE_USING = 4;
    public static final int FLAG_AUTO_TRIM = 8;

    private boolean splitMode;
    private String encoding = DEFAULT_ENCODING;
    private boolean parseFieldNames;
    private String delimiter = DEFAULT_DELIMITER;
    private String quote = DEFAULT_QUOTE;
    private String ignoreCharsNearDelimiter = DEFAULT_IGNORE_CHARS_NEAR_DELIMITER;
    private boolean escapeUsing;
    private boolean autoTrim = true;

    public CSVParams(String encoding, String delimiter, String quote, String ignoreCharsNearDelimiter, int flags) {
        this.splitMode = (flags & FLAG_SPLIT_MODE) != 0;
        setEncoding(encoding);
        this.parseFieldNames = (flags & FLAG_PARSE_FIELD_NAMES) != 0;
        setDelimiter(delimiter);
        setQuoteString(quote);
        setIgnoreCharsNearDelimiter(ignoreCharsNearDelimiter);
        this.escapeUsing = (flags & FLAG_ESCAPE_USING) != 0;
        this.autoTrim = (flags & FLAG_AUTO_TRIM) != 0;
    }

    public static int flags(boolean splitMode, boolean parseFieldNames, boolean escapeUsing, boolean autoTrim) {
        int res = 0;
        if (splitMode) res |= FLAG_SPLIT_MODE;
        if (parseFieldNames) res |= FLAG_PARSE_FIELD_NAMES;
        if (escapeUsing) res |= FLAG_ESCAPE_USING;
        if (autoTrim) res |= FLAG_AUTO_TRIM;
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

    @SuppressWarnings("unused")
    public final void setQuoteString(String value) {
        this.quote = nullOrEmpty(StringEscapeUtils.unescapeJava(value), DEFAULT_QUOTE);
    }

    public final char getQuoteChar() {
        return quote.charAt(0);
    }

    @SuppressWarnings("unused")
    public final void setIgnoreCharsNearDelimiter(String value) {
        this.ignoreCharsNearDelimiter = nullOrEmpty(StringEscapeUtils.unescapeJava(value), DEFAULT_IGNORE_CHARS_NEAR_DELIMITER);
    }

    public final char getDelimiter() {
        return delimiter.charAt(0);
    }

    public final boolean isDelimiter(char value) {
        return delimiter.indexOf(value) >= 0;
    }

    public final boolean isIgnoredChar(char value) {
        return ignoreCharsNearDelimiter.indexOf(value) >= 0;
    }
}
