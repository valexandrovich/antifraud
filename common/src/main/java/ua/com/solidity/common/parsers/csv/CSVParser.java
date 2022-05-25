package ua.com.solidity.common.parsers.csv;

import lombok.CustomLog;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.data.DataHeader;
import ua.com.solidity.common.data.DataObject;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;


@CustomLog
public class CSVParser extends CustomParser {
    private Scanner scanner;
    private DataHeader header;
    public final Map<String, Integer> fieldNames = new HashMap<>();
    public final List<String> dataFields = new ArrayList<>();
    private final StringBuilder mBuilder = new StringBuilder();
    private int colCount = -1;
    private final ParseRowData data;

    private static class ParseRowData {
        int fieldStart = 0;
        int lineNumber = 0;
        int lineColumn = 0;
        int dataLineNumber = 0;
        final List<String> cache = new ArrayList<>();
        String row;
        boolean quoted = false;
        int start = 0;
        int offset = 0;
        char delimiter = 0;
        CSVParams params;

        ParseRowData(CSVParams params) {
            this.params = params;
        }

        public final void startDataRow(String row) {
            ++lineNumber;
            dataLineNumber = lineNumber;
            lineColumn = 0;
            start = offset = 0;
            delimiter = 0;
            cache.clear();
            cache.add(row);
            this.row = row;
        }

        public final void addRow(String row) {
            cache.add(row);
            this.row += params.lineSeparator + row;
            ++lineNumber;
            lineColumn = 0;
            offset += params.lineSeparator.length();
        }

        boolean notEOL() {
            return offset < row.length();
        }

        void ignoreCharsNearDelimiter() {
            while (notEOL() && isNotDelimiter() && params.isIgnoredChar(row.charAt(offset))) {
                ++offset;
                ++start;
                ++lineColumn;
            }
        }

        char getChar() {
            return row.charAt(offset);
        }

        void next() {
            ++offset;
        }

        void align() {
            start = offset;
        }

        String delta() {
            return row.substring(start, offset);
        }

        final boolean isQuote() {
            return isQuote(getChar());
        }

        final boolean isQuote(char ch) {
            return params.getQuoteChar() == ch;
        }

        final boolean isEscape() {
            return isEscape(getChar());
        }

        final boolean isEscape(char ch) {
            return params.getEscapeChar() == ch;
        }

        boolean isNotDelimiter() {
            return isNotDelimiter(getChar());
        }

        boolean isNotDelimiter(char ch) {
            if (delimiter != 0) {
                return ch != delimiter;
            }
            if (params.isDelimiter(ch)) {
                delimiter = ch;
                return false;
            }
            return true;
        }

        void ignore() {
            while (notEOL() && isNotDelimiter()) next();
        }

        void fieldStartSeek() {
            quoted = false;
            ignoreCharsNearDelimiter();
            fieldStart = offset;
        }
        @SuppressWarnings("unused")
        List<String> getCache() {
            return cache;
        }
    }

    public CSVParser(CSVParams params) {
        data = new ParseRowData(params == null ? new CSVParams() : params);
    }

    private String initFieldName(String fieldName) {
        fieldName = fieldName == null ? "" : fieldName.trim();
        if (fieldName.isEmpty() || fieldName.startsWith("#")) {
            fieldName = "#" + (fieldNames.size() + 1);
            fieldNames.put(fieldName, fieldNames.size());
        } else {
            if (fieldNames.containsKey(fieldName)) {
                int delta = 1;
                String newFieldName;
                do {
                    newFieldName = fieldName + "_" + delta++;
                } while (fieldNames.containsKey(newFieldName));
                fieldName = newFieldName;
            }
        }
        return fieldName;
    }

    @Override
    protected boolean doOpen() {
        InputStreamReader reader = new InputStreamReader(stream,
                Charset.availableCharsets().getOrDefault(data.params.getEncoding(), StandardCharsets.UTF_8));
        scanner = new Scanner(reader);

        if (data.params.isParseFieldNames()) {
            if (!parseRow()) return false;
            for (int i = 0; i < dataFields.size(); ++i) {
                fieldNames.put(initFieldName(dataFields.get(i)), i);
            }
        }
        return true;
    }

    private void recoverValue(StringBuilder builder, String row, int fieldStart, int fieldEnd) {
        int fieldEndCache = fieldEnd;
        try {
            while (fieldEnd > fieldStart && data.params.isIgnoredChar(row.charAt(fieldEnd - 1))) --fieldEnd;
            if (data.isQuote(row.charAt(fieldStart)) && fieldEnd > fieldStart && data.isQuote(row.charAt(fieldEnd - 1))) { // fieldEnd-1 changed to fieldEnd
                String content = data.params.recoverValue(row.substring(fieldStart + 1, fieldEnd - 1));
                if (content.length() != builder.length()) {
                    builder.setLength(0);
                    builder.append(content);
                }
            }
        } catch (Exception e) {
            log.error("Recovery error: builderData: {}\nrowData:{}\nfieldStart:{}, fieldEnd:{}", builder.toString(), row, fieldStart, fieldEndCache, e);
        }
    }

    private String trimByIgnoredChars(String value) {
        int removeBefore = 0;
        int removeAfter = 0;

        for (int i = 0; i < value.length(); ++i) {
            if (data.params.isIgnoredChar(value.charAt(i))) ++removeBefore;
            else break;
        }

        if (removeBefore < value.length()) {
            for (int i = value.length() - 1; i >= 0; --i) {
                if (data.params.isIgnoredChar(value.charAt(i))) ++removeAfter;
                else break;
            }
        }

        return value.substring(removeBefore, value.length() - removeAfter);
    }

    private void doSplitRow() {
        String[] items = data.row.split(String.valueOf(data.params.getDelimiter()));
        String quote = String.valueOf(data.params.getQuoteChar());
        for (int i = 0; i < items.length; ++i) {
            String item = trimByIgnoredChars(items[i]);
            boolean isQuoted = item.startsWith(quote) && item.endsWith(quote) && item.length() > 1;
            if (isQuoted) {
                item = item.substring(1, item.length() - 1).replace(quote + quote, quote);
                items[i] = item;
            }
            pushField(item, isQuoted);
        }
    }

    private boolean doHandleEscapeCharInQuotedMode(ParseRowData data) {
        boolean res = true;
        if (data.notEOL()) { // EOL or escape char found
            data.next();
            if (data.notEOL() && data.isQuote()) {
                mBuilder.append(data.getChar());
                data.next();
                data.align();
            } else {
                data.align();
                res = false;
            }
        } else {
            if (scanner.hasNextLine()) {
                data.addRow(scanner.nextLine());
                mBuilder.append(data.params.lineSeparator);
            } else {
                res = false; // erroneous break
            }
        }
        return res;
    }

    private boolean doHandleEscapeCharInEscapeMode(ParseRowData data) {
        boolean res = true;
        if (data.notEOL()) { // EOL or escape char found
            data.next();
            if (data.notEOL()) {
                if (data.params.isEscapeCharNeeded()) {
                    mBuilder.append(data.params.getEscapeChar());
                }
                mBuilder.append(data.getChar());
                data.next();
                data.align();
            } else {
                if (scanner.hasNextLine()) {
                    data.addRow(scanner.nextLine());
                    mBuilder.append(data.params.lineSeparator);
                } else {
                    res = false; // erroneous break
                }
            }
        } else {
            if (scanner.hasNextLine()) {
                data.addRow(scanner.nextLine());
                mBuilder.append(data.params.lineSeparator);
            } else {
                res = false; // erroneous break
            }
        }
        return res;
    }

    private boolean doHandleEscapeValueChar(ParseRowData data) {
        return data.params.doubleQuoteMode() ? doHandleEscapeCharInQuotedMode(data) : doHandleEscapeCharInEscapeMode(data);
    }

    private void doParseQuotedValue(ParseRowData data) {
        boolean tryToContinue = true;
        while (tryToContinue) {
            while (data.notEOL() && !data.isEscape() && !data.isQuote()) data.next();
            mBuilder.append(data.delta());
            tryToContinue = (!data.notEOL() || data.isEscape()) && doHandleEscapeValueChar(data);
        }
        data.ignore(); // ignore all characters, but delimiter
    }

    private void doParseField(ParseRowData data) {
        if (data.isQuote()) { // quoted field
            data.quoted = true;
            data.next();
            data.start = data.offset;
            doParseQuotedValue(data);
        } else {                               // non-quoted field parsing
            int finish = -1;
            while (data.notEOL() && data.isNotDelimiter()) {
                if (data.params.isIgnoredChar(data.getChar())) {
                    if (finish < 0) finish = data.offset;
                } else finish = -1;
                data.next();
            }
            mBuilder.append(data.row.subSequence(data.start, finish < 0 ? data.offset : finish));
        }
    }

    private void doParseRow() {
        while (data.notEOL()) {
            data.fieldStartSeek();
            mBuilder.setLength(0);
            if (data.notEOL()) doParseField(data);
            recoverValue(mBuilder, data.row, data.fieldStart, data.offset);
            pushField(mBuilder.toString(), data.quoted);
            if (!data.notEOL()) break;
            data.next();
            data.start = data.offset;
            if (!data.notEOL()) {  // last empty value
                pushField(null, false);
            }
        }
    }

    private boolean parseRow() {
        dataFields.clear();
        if (!scanner.hasNextLine()) {
            return false;
        }
        data.startDataRow(scanner.nextLine());

        if (data.params.isSplitMode()) {
            doSplitRow();
        } else {
            doParseRow();
        }

        if (colCount < 0) {
            colCount = dataFields.size();
        } else {
            if (dataFields.size() != colCount) {
                StringBuilder errorBuilder = new StringBuilder();
                for (int i = 0; i < data.cache.size(); ++i) {
                    if (i > 0) errorBuilder.append("\n");
                    errorBuilder.append(data.cache.get(i));
                }
                errorReporting(data.lineNumber, data.offset, -1, -1, -1, errorBuilder.toString());
            }
        }

        return true;
    }

    private void headerNeeded() {
        if (header == null) {
            if (fieldNames.isEmpty()) {
                for (int i = 0; i < dataFields.size(); ++i) {
                    initFieldName(null);
                }
            }
            header = new DataHeader(fieldNames);
        }
    }

    @Override
    protected DataObject internalDataObject() {
        headerNeeded();
        return CSVDataObject.create(header, dataFields, data.lineNumber, data.offset);
    }

    protected boolean doNext() {
        return parseRow();
    }

    private void pushField(String field, boolean quoted) {
        if (field != null) {
            if (data.params.isAutoTrim()) {
                field = field.trim();
            }
            if (field.length() == 0) {
                if (quoted) {
                    field = "";
                } else {
                    field = null;
                }
            }
        }
        dataFields.add(field);
    }
}
