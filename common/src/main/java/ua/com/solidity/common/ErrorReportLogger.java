package ua.com.solidity.common;

import ua.com.solidity.common.data.DataLocation;
import java.util.Objects;

public abstract class ErrorReportLogger {
    protected String source;
    protected DataLocation location;
    protected String info;
    private long errorCount = 0;

    protected abstract void handleNewSource(String source);
    protected abstract void handleNewLine(DataLocation location, String info);
    protected abstract void handleLineReport(DataLocation location, String clarification);
    protected abstract void doFinish(long errorCount);

    public final void logError(ErrorReport report) {
        boolean sourceChanged = Objects.equals(report.getSource(), source);
        if (location == null || sourceChanged || location.getRow() != report.getLocation().getRow()) {
            if (sourceChanged) {
                handleNewSource(report.getSource());
            }
            location = report.getLocation();
            info = report.getInfo();
            handleNewLine(location, info);
        }
        handleLineReport(report.getLocation(), report.getClarification());
        ++errorCount;
    }

    public final void finish() {
        doFinish(errorCount);
    }

}
