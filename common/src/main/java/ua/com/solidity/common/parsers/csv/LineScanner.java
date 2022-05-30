package ua.com.solidity.common.parsers.csv;

import lombok.CustomLog;

import java.io.InputStreamReader;
import java.nio.CharBuffer;

@CustomLog
public class LineScanner {
    private static final int CHAR_BUFFER_SIZE = 8192;
    private static final int DEFAULT_MAX_LINE_SIZE = 4 * 1024 * 1024; // 8 Mb
    private final InputStreamReader reader;
    private final CharBuffer currentBuffer = CharBuffer.allocate(CHAR_BUFFER_SIZE);
    private final StringBuilder builder = new StringBuilder();
    private final int maxLineSize;
    private boolean readingError = false;
    private boolean waitForLineFeed = false;
    private boolean rowLengthIsTooLong = false;
    private long row = 0;

    private boolean eof = false;

    public LineScanner(InputStreamReader reader, int maxLineSize) {
        this.maxLineSize = maxLineSize <= 0 ? DEFAULT_MAX_LINE_SIZE : maxLineSize;
        this.reader = reader;
        loadCurrentBuffer();
    }

    public final long getRow() {
        return row;
    }

    public final long getMaxLineSize() {
        return maxLineSize;
    }

    public final boolean rowLengthIsTooLong() {
        return rowLengthIsTooLong;
    }

    private void loadCurrentBuffer() {
        currentBuffer.clear();
        if (eof) return;
        int count = 0;
        while (!readingError && count >= 0 && currentBuffer.hasRemaining()) {
            try {
                count = reader.read(currentBuffer);
            } catch (Exception e) {
                log.error("CSV Data reading error.", e);
                readingError = true;
            }
        }

        currentBuffer.rewind();
        eof = count < 0;
    }

    private void flushData(int first) {
        int len = currentBuffer.position() - first;
        if (len <= 0) return;
        int copyLen = Math.min(len, maxLineSize - builder.length());
        if (copyLen > 0) {
            builder.append(currentBuffer.array(), first, copyLen);
        }
        rowLengthIsTooLong |= copyLen < len;
    }

    public final String nextLine() {
        rowLengthIsTooLong = false;
        ++row;
        boolean completed = false;
        while (!completed) {
            int first = currentBuffer.position();

            while (currentBuffer.hasRemaining()) {
                char ch = currentBuffer.get();
                switch (ch) {
                    case '\r':
                        waitForLineFeed = true;
                        completed = true;
                        break;
                    case '\u2028':
                    case '\u2029':
                    case '\u0085':
                        completed = true;
                        break;
                    case '\n':
                        if (waitForLineFeed) {
                            waitForLineFeed = false;
                        } else {
                            completed = true;
                        }
                        break;
                    default:
                }
            }

            flushData(first);

            if (!completed && !eof) {
                loadCurrentBuffer();
            } else {
                completed = true;
            }
        }

        return builder.toString();
    }

    public final boolean hasNextLine() {
        return !eof && currentBuffer.hasRemaining();
    }
}
