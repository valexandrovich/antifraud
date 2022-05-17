package ua.com.solidity.common;
import ua.com.solidity.common.data.DataObject;

import java.io.Closeable;
import java.io.InputStream;

public abstract class CustomParser implements Closeable {
    protected InputStream stream = null;
    private boolean eof = false;
    private boolean opened = false;
    private DataObject cachedDataObject = null;
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
    protected abstract DataObject internalDataObject();
    protected abstract boolean doNext();

    public void close() {
        errorReport = null;
        opened = false;
        cachedDataObject = null;
        eof = true;
        stream = null;
    }

    public final void next() {
        errorReport = null;
        cachedDataObject = null;
        eof = !doNext();
    }

    public final DataObject dataObject() {
        if (errorReport != null || !hasData()) return null;
        if (cachedDataObject == null) {
            cachedDataObject = internalDataObject();
        }
        return cachedDataObject;
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

    public final void errorReporting(long row, long col, long byteOffset, long charOffset, long infoOffset, String info) {
        errorReport = new ErrorReport(row, col, byteOffset, charOffset, infoOffset, info, "Parse error");
    }

    public final void errorReporting(ErrorReport report) {
        errorReport = report;
    }
}
