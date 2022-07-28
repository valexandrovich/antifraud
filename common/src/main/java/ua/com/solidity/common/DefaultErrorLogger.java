package ua.com.solidity.common;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import ua.com.solidity.common.data.DataLocation;

@CustomLog
public class DefaultErrorLogger extends ErrorReportLogger {
    public static final long MAX_ROW_COUNT_FOR_MEMORY_MODE = 10000;
    private final String fileName;
    private final String mailto;
    private OutputStream stream = null;
    private OutputStreamWriter writer = null;
    private long maxRowCount;
    private long rowCount = 0;
    private boolean error = false;

    @Getter
    @Setter
    private String service = "";

    @Getter
    @Setter
    private String subject = "Error report";

    public DefaultErrorLogger(String fileName, String mailto, long maxRowCount) {
        this.fileName = fileName;
        this.mailto = mailto;
        this.maxRowCount = maxRowCount;
    }

    public DefaultErrorLogger(String fileName, String mailto, long maxRowCount, String subject) {
        this.fileName = fileName;
        this.mailto = mailto;
        this.maxRowCount = maxRowCount;
        this.subject = subject;
    }

    private boolean streamNeeded() {
        if (writer != null || error) return !error;

        error = true;
        try {
            if (fileName == null || fileName.isBlank() || StringUtils.equals(fileName, "?")) {
                stream = new ByteArrayOutputStream();
                if (maxRowCount > MAX_ROW_COUNT_FOR_MEMORY_MODE || maxRowCount < 0)
                    maxRowCount = MAX_ROW_COUNT_FOR_MEMORY_MODE;
            } else {
                stream = new FileOutputStream(fileName);
            }
            writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
            error = false;
        } catch (Exception e) {
            log.error("Can't create file {}. ", fileName, e);
        }
        return true;
    }

    private void doWrite(String str) {
        if (streamNeeded()) {
            try {
                writer.write(str);
            } catch (Exception e) {
                // nothing
            }
        }
    }

    @Override
    protected void handleNewSource(String source) {
        doWrite(Utils.messageFormat("\n***** Source: {} *****\n\n", source));
    }

    @Override
    protected void handleNewLine(DataLocation location, String info) {
        if (rowCount >= maxRowCount && maxRowCount > 0) {
            if (rowCount == maxRowCount) {
                doWrite("\n\n<too many rows>\n");
                ++rowCount;
            }
            return;
        }
        doWrite(location.toString() + ":\n" + info + "\n");
        ++rowCount;
    }

    @Override
    protected void handleLineReport(DataLocation location, String clarification) {
        doWrite("\t at:" + location.toString() + ", " + clarification + System.lineSeparator());
    }

    @Override
    protected void doFinish(long errorCount) {
        if (writer != null) {
            try {
                writer.close();
                stream.close();
            } catch(Exception e) {
                // nothing
            }
            if (mailto != null && !mailto.isBlank()) {
                String targetSubject = (service == null || service.isBlank() ? "" : service + ": ") + subject;
                NotificationMessage msg;
                if (stream instanceof ByteArrayOutputStream) {
                    msg = new NotificationMessage(mailto, targetSubject,
                            ((ByteArrayOutputStream) stream).toString(StandardCharsets.UTF_8), 3, null);
                } else {
                    msg = new NotificationMessage(mailto, targetSubject, "", 3, fileName);
                }
                Utils.sendRabbitMQMessage(OtpExchange.NOTIFICATION, msg);
            }
        }
    }
}
