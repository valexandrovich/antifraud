package ua.com.solidity.common.parsers.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ua.com.solidity.common.CustomParser;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVParser extends CustomParser {
    private final CSVParams params;
    private Scanner scanner;
    private boolean mEOF = false;
    private long lineNumber = 0;

    public final Map<String, Integer> fieldNames = new HashMap<>();

    public final List<String> dataFields = new ArrayList<>();

    private final StringBuilder mBuilder = new StringBuilder();
    private final List<String> cache = new ArrayList<>();
    private int colCount = -1;

    public CSVParser(CSVParams params) {
        this.params = params == null ? new CSVParams() : params;
    }

    @Override
    protected boolean doOpen() {
        InputStreamReader reader = new InputStreamReader(stream,
                Charset.availableCharsets().getOrDefault(params.getEncoding(), StandardCharsets.UTF_8));
        scanner = new Scanner(reader);
        mEOF = false;
        lineNumber = 0;

        if (params.isParseFieldNames()) {
            if (!parseRow()) return false;
            for (int i = 0; i < dataFields.size(); ++i) fieldNames.put(dataFields.get(i), i);
        }
        mEOF = !parseRow();
        return true;
    }
    @SuppressWarnings("unused")
    public final String getFieldByName(String name) {
        if (fieldNames.containsKey(name)) {
            int index = fieldNames.get(name);
            if (dataFields.size() > index) return dataFields.get(index);
        }
        return null;
    }

    private String getFieldName(int index) {
        for (Map.Entry<String, Integer> entry : fieldNames.entrySet()) {
            if (entry.getValue() == index) return entry.getKey();
        }
        String name = generateName();
        fieldNames.put(name, index);
        return name;
    }

    private void recoverValue(StringBuilder builder, String row, int fieldStart, int fieldEnd) {
        while (fieldEnd > 0 && fieldEnd < row.length() && params.isIgnoredChar(row.charAt(fieldEnd))) --fieldEnd;
        if (row.charAt(fieldStart) == params.getQuoteChar() && row.charAt(fieldEnd - 1) == params.getQuoteChar()) {
            String content = row.substring(fieldStart + 1, fieldEnd - 1).replace(String.valueOf(params.getQuoteChar()) + params.getQuoteChar(), String.valueOf(params.getQuoteChar()));
            if (content.length() != builder.length()) {
                builder.setLength(0);
                builder.append(content);
            }
        }
    }

    private String trimByIgnoredChars(String value) {
        int removeBefore = 0;
        int removeAfter = 0;

        for (int i = 0; i < value.length(); ++i) {
            if (params.isIgnoredChar(value.charAt(i))) ++removeBefore;
            else break;
        }

        if (removeBefore < value.length() - 1) {
            for (int i = value.length() - 1; i >= 0; --i) {
                if (params.isIgnoredChar(value.charAt(i))) ++removeAfter;
            }
        }

        return value.substring(removeBefore, value.length() - removeAfter - 1);
    }

    private void doSplitParseRow(String row) {
        String[] items = row.split(String.valueOf(params.getDelimiter()));
        String quote = String.valueOf(params.getQuoteChar());
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

    private static class ParseRowData {
        int fieldStart = 0;
        String row;
        boolean quoted = false;
        int start = 0;
        int offset = 0;
        CSVParams params;

        ParseRowData(CSVParams params, String row) {
            this.params = params;
            this.row = row;
        }

        boolean notEOL() {
            return offset < row.length();
        }

        void ignoreCharsNearDelimiter() {
            while (notEOL() && params.isIgnoredChar(row.charAt(offset))) {
                ++offset;
                ++start;
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

        boolean isQuote() {
            return getChar() == params.getQuoteChar();
        }

        boolean isNotDelimiter() {
            return getChar() != params.getDelimiterChar();
        }

        void ignore() {
            while (notEOL() && isNotDelimiter()) next();
        }

        void resetStartAndOffset() {
            start = offset = 0;
        }

        void fieldStartSeek() {
            quoted = false;
            ignoreCharsNearDelimiter();
            fieldStart = offset;
        }
    }

    private boolean doHandleParseQuotedValue(ParseRowData data) {
        boolean res = true;
        if (data.notEOL()) { // quote found
            data.next();
            if (data.notEOL() && data.isQuote()) { // another quote found
                mBuilder.append(params.getQuoteChar());
                data.next();
                data.align();
            } else {
                data.align();
                res = false;
            }
        } else {
            data.resetStartAndOffset();
            if (scanner.hasNextLine()) {
                data.row = scanner.nextLine();
                ++lineNumber;
                cache.add(data.row);
                mBuilder.append('\n');
            } else {
                res = false; // erroneous break
            }
        }
        return res;
    }

    private void doParseQuotedValue(ParseRowData data) {
        boolean tryToContinue = true;
        while (tryToContinue) {
            while (data.notEOL() && !data.isQuote()) data.next();
            mBuilder.append(data.delta());
            tryToContinue = doHandleParseQuotedValue(data);
        }
        data.ignore(); // ignore all characters, but delimiter
    }

    private void doParseFields(ParseRowData data) {
        if (data.isQuote()) { // quoted field
            data.quoted = true;
            data.next();
            data.start = data.offset;
            doParseQuotedValue(data);
        } else {                               // non-quoted field parsing
            int finish = -1;
            while (data.notEOL() && data.isNotDelimiter()) {
                if (params.isIgnoredChar(data.getChar())) {
                    if (finish < 0) finish = data.offset;
                } else finish = -1;
                data.next();
            }
            mBuilder.append(data.row.subSequence(data.start, finish < 0 ? data.offset : finish));
        }
    }

    private void doParseRow(String row) {
        ParseRowData data = new ParseRowData(params, row);
        while (data.notEOL()) {
            data.fieldStartSeek();
            mBuilder.setLength(0);
            if (data.notEOL()) doParseFields(data);
            recoverValue(mBuilder, row, data.fieldStart, data.offset);
            pushField(mBuilder.toString(), data.quoted);
            if (!data.notEOL()) break;
            data.next();
            data.start = data.offset;
        }
    }

    private boolean parseRow() {
        String row;
        clearError();
        dataFields.clear();
        cache.clear();
        if (!scanner.hasNextLine()) {
            mEOF = true;
            return false;
        }

        row = scanner.nextLine();
        ++lineNumber;
        cache.add(row);

        if (params.isSplitMode()) {
            doSplitParseRow(row);
        } else {
            doParseRow(row);
        }

        if (colCount < 0) {
            colCount = dataFields.size();
        } else {
            if (dataFields.size() != colCount) {
                StringBuilder errorBuilder = new StringBuilder();
                for (int i = 0; i < cache.size(); ++i) {
                    if (i > 0) errorBuilder.append("\n");
                    errorBuilder.append(cache.get(i));
                }
                errorReporting(lineNumber, -1, -1, -1, -1, errorBuilder.toString());
            }
        }

        return true;
    }

    @Override
    public JsonNode getNode() {
        ObjectNode res = JsonNodeFactory.instance.objectNode();
        for (int i = 0; i < dataFields.size(); ++i) {
            res.put(getFieldName(i), dataFields.get(i)); // use ValueParser.parse
        }
        return res;
    }

    public final boolean hasData() {
        return !mEOF;
    }

    protected boolean doNext() {
        if (!mEOF) parseRow();
        return mEOF;
    }

    private void pushField(String field, boolean quoted) {
        if (field != null && field.length() == 0) {
            if (quoted) dataFields.add("");
            else dataFields.add(null);
        } else dataFields.add(field);
    }
}
