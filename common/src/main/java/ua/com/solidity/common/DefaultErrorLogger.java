package ua.com.solidity.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
public class DefaultErrorLogger extends ErrorReportLogger {
    private final String fileName;
    private final String mailto;
    private FileOutputStream stream = null;
    private OutputStreamWriter writer = null;
    private final long maxRowCount;
    private long rowCount = 0;
    private boolean error = false;

    @AllArgsConstructor
    @Getter
    @Setter
    private static class NotificationMessage {
        private String to;
        private String subject;
        private String body;
        private int retries;
        private String filePath;
    }

    public DefaultErrorLogger(String fileName, String mailto, long maxRowCount) {
        this.fileName = fileName;
        this.mailto = mailto;
        this.maxRowCount = maxRowCount;
    }

    private boolean streamNeeded() {
        if (writer != null || error) return false;
        try {
            stream = new FileOutputStream(fileName);
            writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Can't create file {}. {}: {}", fileName, e.getClass().getName(), e.getMessage());
            log.debug("Exception:", e);
            error = true;
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
    protected void handleNewLine(ErrorReport.Location location, String info) {
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
    protected void handleLineReport(ErrorReport.Location location, String clarification) {
        doWrite("\t at:" + location.toString() + ", " + clarification);
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
            if (mailto != null) {
                NotificationMessage msg = new NotificationMessage(mailto, "Importer error report", "", 3, fileName);
                Utils.sendRabbitMQMessage("otp-etl.notification", Utils.objectToJsonString(msg));
            }
        }
    }
}