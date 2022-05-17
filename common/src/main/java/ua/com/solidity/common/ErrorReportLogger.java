package ua.com.solidity.common;

import ua.com.solidity.common.data.DataLocation;

public abstract class ErrorReportLogger {
    protected DataLocation location;
    protected String info;
    private long errorCount = 0;

    protected abstract void handleNewLine(DataLocation location, String info);
    protected abstract void handleLineReport(DataLocation location, String clarification);
    protected abstract void doFinish(long errorCount);

    public final void logError(ErrorReport report) {
        if (location == null || location.getRow() != report.getLocation().getRow()) {
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
