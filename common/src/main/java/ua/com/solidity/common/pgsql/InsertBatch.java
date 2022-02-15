package ua.com.solidity.common.pgsql;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.impl.dv.util.Base64;
import org.springframework.lang.NonNull;
import ua.com.solidity.common.ValueParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
public class InsertBatch {
    public static final String NULL_STR = "null";

    public interface Handler {
        int handleBatch(InsertBatch batch, CharBuffer buffer, int count);
    }

    public static class BatchReader extends Reader {
        private boolean closed = false;
        private final InsertBatch batch;

        public BatchReader(InsertBatch batch) {
            this.batch = batch;
        }

        @Override
        public int read(@NonNull char[] charBuf, int off, int len) throws IOException {
            len = closed ? 0 : Math.min(len, batch.buffer.remaining());
            if (len > 0) {
                batch.buffer.get(charBuf, off, len);
            }
            return len;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        public final void batchReset() {
            closed = false;
        }
    }

    private static class Format {
        public final String quote;
        public final String replaceStrFrom;
        public final String replaceStrTo;
        public final boolean parenthesis;
        public final boolean suffix;
        public final String rowDelimiter;

        public Format(String quote, String replaceStrFrom, String replaceStrTo, String rowDelimiter, boolean parenthesis, boolean suffix) {
            this.quote = quote;
            this.replaceStrFrom = replaceStrFrom;
            this.replaceStrTo = replaceStrTo;
            this.rowDelimiter = rowDelimiter;
            this.parenthesis = parenthesis;
            this.suffix = suffix;
        }

        public final boolean pushRow(CharBuffer target, CharBuffer row, int currentRowCount) {
            if (target.remaining() >= row.length() + (parenthesis ? 2 : 0) + (currentRowCount > 0 ? rowDelimiter.length() : 0)) {
                if (currentRowCount > 0) {
                    target.append(rowDelimiter);
                }
                if (parenthesis) {
                    target.append("(").append(row).append(")");
                } else {
                    target.append(row);
                }
                row.clear();
                return true;
            }
            return false;
        }
    }

    private final CharBuffer buffer;
    private final CharBuffer row;
    private final Handler handler;
    private final int startPosition;
    private boolean badRow = false;
    private int rowCount;
    private int colCount;
    private final Format format;
    private final BatchReader reader;

    private InsertBatch(int cacheSize, int rowCacheBufferSize, String header, Handler handler, boolean insertMode) {
        this.handler = handler;
        if (insertMode) {
            format = new Format("'", "'", "''", ",", true, true);
        } else {
            format = new Format("\"", "\"", "\"\"", "\n", false, false);
        }
        buffer = ByteBuffer.allocateDirect(cacheSize * 2).asCharBuffer();
        if (insertMode) {
            buffer.append(header);
            startPosition = buffer.position();
            reader = null;
        } else {
            startPosition = 0;
            reader = new BatchReader(this);
        }
        row = ByteBuffer.allocateDirect(rowCacheBufferSize * 2).asCharBuffer();
        rowCount = 0;
        colCount = 0;
    }

    private static InsertBatch doAllocate(int cacheSize, int rowCacheSize, String header, Handler handler, boolean insertMode) {
        if (cacheSize <= 0 || rowCacheSize <= 0 || handler == null || header == null || header.isBlank()) return null;
        if (cacheSize < rowCacheSize + header.length()) {
            cacheSize = Math.max(rowCacheSize + header.length(), rowCacheSize * 2);
        }
        return new InsertBatch(cacheSize, rowCacheSize, header, handler, insertMode);
    }

    public static InsertBatch allocate(int cacheSize, int rowCacheSize, String header, Handler handler) {
        return doAllocate(cacheSize, rowCacheSize, header, handler, false);
    }

    public static InsertBatch allocate(int cacheSize, int rowCacheSize, Handler handler) {
        return doAllocate(cacheSize, rowCacheSize, null, handler, true);
    }

    public final boolean addBatch() {
        colCount = 0;
        if (!badRow) {
            row.flip();
            if (format.pushRow(buffer, row, rowCount)) {
                ++rowCount;
            } else {
                flush();
                addBatch();
            }
            return true;
        } else {
            badRow = false;
            row.clear();
        }
        return false;
    }

    public final void resetBatch() {
        row.clear();
    }

    public final Reader getReader() {
        return reader;
    }

    public final int bufferSize() {
        return buffer.limit();
    }

    public final void flush() {
        if (rowCount == 0) return;
        buffer.flip();
        if (reader != null) reader.batchReset();
        int cacheRowCount = rowCount;
        rowCount = 0;
        handler.handleBatch(this, buffer, cacheRowCount);
        buffer.clear();
        buffer.position(startPosition);
    }

    private void put(String text) {
        if (badRow || text.isBlank()) return;
        badRow = row.remaining() < text.length();
        if (!badRow) {
            row.put(text);
        }
    }

    public final void putString(String value) {
        putString(value, "");
    }

    public final void putString(String value, String suffix) {
        if (colCount > 0) {
            put(",");
        }
        if (value == null) {
            put(NULL_STR);
        } else {
            put(format.quote);
            put(value.replace(format.replaceStrFrom, format.replaceStrTo));
            put(format.quote);
            if (format.suffix) put(suffix);
        }
        ++colCount;
    }

    public final void putDate(LocalDate date) {
        putString(date == null ? null : ValueParser.formatLocalDate(date), "::DATE");
    }

    public final void putDateTime(LocalDateTime datetime) {
        putString(datetime == null ? null : ValueParser.formatLocalDateTime(datetime), "::TIMESTAMP");
    }

    public final void putZonedDate(ZonedDateTime datetime) {
        putString(datetime == null ? null : ValueParser.formatZonedDate(datetime), "::DATE WITH TIME ZONE");
    }

    @SuppressWarnings("unused")
    public final void putZonedDateTime(ZonedDateTime datetime) {
        putString(datetime == null ? null : ValueParser.formatZonedDate(datetime), "::TIMESTAMP WITH TIME ZONE");
    }

    public final void putInt(Long value) {
        if (colCount > 0) {
            put(",");
        }
        if (value == null) {
            put(NULL_STR);
        } else {
            put(value.toString());
        }
        ++colCount;
    }

    @SuppressWarnings("unused")
    public final void putFloat(Double value) {
        if (colCount > 0) {
            put(",");
        }
        if (value == null) {
            put(NULL_STR);
        } else {
            put(value.toString());
        }
        ++colCount;
    }

    @SuppressWarnings("unused")
    public final void putBoolean(Boolean value) {
        if (colCount > 0) {
            put(",");
        }
        if (value == null) {
            put(NULL_STR);
        } else {
            put(value.equals(true) ? "true" : "false");
        }
        ++colCount;
    }

    public final void putUUID(UUID value) {
        putString(value == null ? null : value.toString(), "::uuid");
    }

    private String byteArrayToString(byte[] value) {
        if (value == null) return null;
        final char[] hexChars = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        CharBuffer buf = CharBuffer.allocate(value.length * 2 + 2);
        buf.put("\\x");
        for (byte b : value) {
            buf.put(hexChars[(b >> 4) & 0xf]);
            buf.put(hexChars[b & 0xf]);
        }
        buf.flip();
        return buf.toString();
    }

    public final void putBytea(byte[] value) {
        putString(byteArrayToString(value), "::bytea");
    }

    public final void putByteaBase64(String base64encodedString) {
        putBytea(base64encodedString == null || base64encodedString.isBlank() ? null : Base64.decode(base64encodedString));
    }

    public final void putJsonNode(JsonNode node) {
        if (colCount > 0) {
            put(",");
        }
        if (node == null || !(node.isObject() || node.isArray())) {
            put(NULL_STR);
        } else {
            putString(node.toString(), "::json");
        }
        ++colCount;
    }

    public final void putJsonNodeBinary(JsonNode node) {
        if (colCount > 0) {
            put(",");
        }
        if (node == null || !(node.isObject() || node.isArray())) {
            put(NULL_STR);
        } else {
            putString(node.toString(), "::jsonb");
        }
        ++colCount;
    }

    public final void putByteaHex(String hexString) {
        if (colCount > 0) {
            put(",");
        }
        if (hexString == null) {
            put(NULL_STR);
        } else {
            put("'\\x");
            put(hexString);
            put("'::bytea");
        }
        ++colCount;
    }

    public final void putText(String text) {
        putString(text, "");
    }
}
