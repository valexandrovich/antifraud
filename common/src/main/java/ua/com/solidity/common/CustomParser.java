package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Closeable;
import java.io.InputStream;

public abstract class CustomParser implements Closeable {
    public static final String PREFIX = "#";
    public static final String DEFAULT_NAMES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private int prefixCounter = 0;
    protected InputStream stream = null;
    private boolean eof = false;
    private boolean opened = false;
    private ErrorReport errorReport = null;

    public boolean open(InputStream stream) {
        if (opened || stream == null) {
            return false;
        }
        this.stream = stream;
        opened = doOpen();
        if (opened) {
            next();
        }
        return opened;
    }

    protected abstract boolean doOpen();
    public abstract JsonNode getNode();
    protected abstract boolean doNext();

    protected String generateName() {
        int counter = prefixCounter;
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX);
        do {
            builder.append(DEFAULT_NAMES.charAt(counter % DEFAULT_NAMES.length()));
            counter /= DEFAULT_NAMES.length();
        } while (counter != 0);
        ++prefixCounter;
        return builder.toString();
    }

    public void close() {
        opened = false;
        stream = null;
    }

    public final void next() {
        clearError();
        eof = !doNext();
    }

    public final boolean hasData() {
        return !eof;
    }

    public final boolean isErrorReporting() {
        return errorReport != null;
    }

    public final ErrorReport getErrorReport() {
        return errorReport;
    }

    protected final void clearError() {
        errorReport = null;
    }

    public final void errorReporting(long row, long col, long byteOffset, long charOffset, long infoOffset, String info) {
        errorReport = new ErrorReport(row, col, byteOffset, charOffset, infoOffset, info, null);
    }

    public final void errorReporting(ErrorReport report) {
        errorReport = report;
    }
}
