package ua.com.solidity.importer;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ua.com.solidity.common.Parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVParser implements Parser {
    public final ua.com.solidity.importer.CSVParams params;
    private final Scanner scanner;
    private boolean mEOF;
    private String mLastRow;
    private boolean mOpened = false;
    private final ObjectNode mNode = JsonNodeFactory.instance.objectNode();
    public final Map<String, Integer> fieldNames = new HashMap<>();

    public final List<String> fields = new ArrayList<>();

    private final StringBuilder mBuilder = new StringBuilder();

    public CSVParser(InputStream stream, ua.com.solidity.importer.CSVParams params) {
        this.params = params;
        InputStreamReader reader = new InputStreamReader(stream,
                Charset.availableCharsets().getOrDefault(params.encoding, StandardCharsets.UTF_8));
        scanner = new Scanner(reader);
        mEOF = false;
        mLastRow = null;
    }

    public final boolean open() {
        if (mOpened) return false;
        if (params.parseFieldNames) {
            if (!parseRow()) return false;
            for (int i = 0; i < fields.size(); ++i) fieldNames.put(fields.get(i), i);
        }
        mOpened = true;
        return parseRow();
    }

    public final String getFieldByName(String name) {
        if (fieldNames.containsKey(name)) {
            int index = fieldNames.get(name);
            if (fields.size() > index) return fields.get(index);
        }
        return null;
    }

    private void recoverValue(StringBuilder builder, String row, int fieldStart, int fieldEnd) {
        while (fieldEnd > 0 && fieldEnd < row.length() && params.ignoreCharsNearDelimiter.indexOf(row.charAt(fieldEnd)) >= 0) --fieldEnd;
        if (row.charAt(fieldStart) == params.quote && row.charAt(fieldEnd - 1) == params.quote) {
            String content = row.substring(fieldStart + 1, fieldEnd - 1).replace(String.valueOf(params.quote) + params.quote, String.valueOf(params.quote));
            if (content.length() != builder.length()) {
                builder.setLength(0);
                builder.append(content);
            }
        }
    }

    private void doSplitParseRow(String row) {
        String[] items = row.split(params.delimiter);
        String quote = String.valueOf(params.quote);
        for (int i = 0; i < items.length; ++i) {
            String item = items[i].trim();
            boolean isQuoted = item.startsWith(quote) && item.endsWith(quote) && item.length() > 1;
            if (isQuoted) {
                item = item.substring(1, item.length() - 2).replace(quote + quote, quote);
                items[i] = item;
            }
            pushField(item, isQuoted);
        }
    }

    private boolean parseRow() {
        mLastRow = "";
        int fieldStart;
        String row;
        boolean quoted;
        int start = 0;
        int offset;
        fields.clear();
        if (!scanner.hasNextLine()) {
            mEOF = true;
            return false;
        }
        row = scanner.nextLine();

        if (params.splitMode) {
            doSplitParseRow(row);
            return true;
        }

        mLastRow = row;
        offset = 0;
        while (offset < row.length()) {
            quoted = false;
            while (offset < row.length() && params.ignoreCharsNearDelimiter.indexOf(row.charAt(offset)) >= 0) {
                ++offset;
                ++start;
            }

            fieldStart = offset;

            mBuilder.setLength(0);

            if (offset < row.length()) {
                // field parsing
                if (row.charAt(offset) == params.quote) { // quoted field
                    quoted = true;
                    ++offset;
                    start = offset;
                    while (true) {
                        while (offset < row.length() && row.charAt(offset) != params.quote) ++offset;
                        mBuilder.append(row.subSequence(start, offset));
                        if (offset < row.length()) { // quote found
                            ++offset;
                            if (offset < row.length() && row.charAt(offset) == params.quote) { // another quote found
                                mBuilder.append(params.quote);
                                ++offset;
                                start = offset;
                                continue;
                            } else {
                                start = offset;
                                break;
                            }
                        } else {
                            start = offset = 0;
                            if (scanner.hasNextLine()) {
                                row = scanner.nextLine();
                                mBuilder.append('\n');
                                mLastRow += params.lineSeparator;
                                mLastRow += row;
                                continue;
                            } else {
                                break; // erroneous break
                            }
                        }
                    }
                    while (offset < row.length() && !row.startsWith(params.delimiter, offset)) ++offset; // ignore all symbols
                } else {                               // non-quoted field parsing
                    int finish = -1;
                    while (offset < row.length() && !row.startsWith(params.delimiter, offset)) {
                        if (params.ignoreCharsNearDelimiter.indexOf(row.charAt(offset)) >= 0) {
                            if (finish < 0) finish = offset;
                        } else finish = -1;
                        ++offset;
                    }
                    mBuilder.append(row.subSequence(start, finish < 0 ? offset : finish));
                }
            }
            recoverValue(mBuilder, row, fieldStart, offset);
            pushField(mBuilder.toString(), quoted);
            if (offset >= row.length()) break;
            offset += params.delimiter.length();
            fieldStart = offset;
            start = offset;
        }

        return true;
    }

    @Override
    public ObjectNode node() {
        mNode.removeAll();
        for (Map.Entry<String, Integer> entry : fieldNames.entrySet()) {
            mNode.put(entry.getKey(), fields.get(entry.getValue()));
        }
        return mNode;
    }

    @Override
    public boolean eof() {
        return mEOF;
    }

    public final String lastRow() {
        return mLastRow;
    }

    @Override
    public void next() {
        if (!mEOF) {
            parseRow();
        }
    }
    private void pushField(String field, boolean quoted) {
        if (field != null && field.length() == 0) {
            if (quoted) fields.add("");
            else fields.add(null);
        } else fields.add(field);
    }
}
