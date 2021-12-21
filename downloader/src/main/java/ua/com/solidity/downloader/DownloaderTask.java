package ua.com.solidity.downloader;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import ua.com.solidity.common.*;
import ua.com.solidity.db.entities.ImportRevision;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.repositories.ImportRevisionRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Slf4j
public class DownloaderTask implements DeferredTask {
    private static final String DOWNLOADER_LOG_DELIMITER = "- - - - - - - - - - - - -";
    private static final String HANDLER = "handler";
    private Receiver receiver;
    private DownloaderMessageData msgData;
    private JsonNode pipelineInfo;
    private JsonNode sourceInfo;
    private ImportSource source;
    private boolean isError = false;
    private int errors = 0;
    private int completed = 0;
    private long tag;
    private final ImportRevisionRepository importRevisionRepository;

    protected DownloaderTask(Receiver receiver, DownloaderMessageData msgData) {
        this.receiver = receiver;
        this.msgData = msgData;
        ApplicationContext context = Utils.getApplicationContext();
        this.importRevisionRepository = context != null ? context.getBean(ImportRevisionRepository.class) : null;
    }

    public final String getPrefix() {
        return source == null ? "" : source.getName() + "-";
    }

    public void handleError() {
        isError = true;
        if (msgData.decrementAttemptsLeft()) {
            Utils.sendRabbitMQMessage(receiver.getConfig().getName(), Utils.objectToJsonString(msgData));
        } else {
            DownloaderErrorMessageData data = new DownloaderErrorMessageData(this.msgData.getIdent());
            RabbitmqLogMessage message = new RabbitmqLogMessage("downloader", "E001", data);
            Utils.sendRabbitMQMessage(receiver.getConfig().getName(), Utils.objectToJsonString(message));
        }
    }

    public final ImporterMessageData sendImportQuery(ImporterInfoFileData data, String dataFileName, String infoFileName) {
        ImportRevision res = new ImportRevision(UUID.randomUUID(), source.getId(), data.getInstant(),
                pipelineInfo, data.getUrl(), dataFileName);
        res = res.save();
        if (res != null) {
            return data.getImporterMessageData(source.getId(), res.getId(), dataFileName, infoFileName, pipelineInfo);
        }
        return null;
    }

    @Override
    public DeferredAction compareWith(DeferredTask task) {
        DownloaderTask otherTask = (DownloaderTask) task;
        return otherTask != null && otherTask.msgData.getIdent().equals(msgData.getIdent()) ? DeferredAction.IGNORE : DeferredAction.APPEND;
    }

    private void doExecuteHandlerInfo(ImporterInfoFileData file, DownloaderTaskHandler handler, DurationPrinter urlElapsedTime, boolean multiple) {
        if (handler.handleImporterInfo(file, this)) {
            log.info("Start downloading from {}", file.getUrl());
            urlElapsedTime.reset();
            ImporterMessageData data = receiver.getDownloader().download(file, this);
            if (multiple) {
                log.info("    - - - - - - - - - - -");
                if (data != null) {
                    log.info("    resource loaded to file {}", data.getDataFileName());
                    ++completed;
                } else {
                    log.error("    Resource not loaded ({})", file.getUrl());
                    ++errors;
                }
                log.info("    elapsed time: {}", urlElapsedTime.getDurationString());
            } else {
                if (data == null) {
                    log.error("Download error.");
                    ++errors;
                }
            }
            urlElapsedTime.stop();
            if (data != null) {
                data.setPipelineInfo(pipelineInfo);
                Utils.sendRabbitMQMessage(receiver.getConfig().getImporterTopicExchangeName(), Utils.objectToJsonString(data));
            }
        }
    }

    private void doExecute(DownloaderTaskHandler handler) {
        ImportRevision lastRevision = ImportRevision.findFirstBySourceName(source.getId());
        List<ImporterInfoFileData> files = handler.getFiles(this);
        if (isError) return;
        if (lastRevision != null && lastRevision.getRevisionDate() != null) {
            files.removeIf(file -> file.getRevisionDateTime().toInstant().compareTo(lastRevision.getRevisionDate()) <= 0);
        }

        if (files.isEmpty()) {
            log.info("New files not found for source name = {}.", source.getName());
            return;
        }

        DurationPrinter elapsedTime = new DurationPrinter();
        DurationPrinter urlElapsedTime = new DurationPrinter();

        files.sort(Comparator.comparing(ImporterInfoFileData::getRevisionDateTime));

        completed = 0;
        errors = 0;

        for (ImporterInfoFileData file : files) {
            doExecuteHandlerInfo(file, handler, urlElapsedTime, files.size() > 1);
        }

        if (files.size() > 1) {
            log.info(DOWNLOADER_LOG_DELIMITER);
            log.info("download completed for {} resources.", completed);
            log.info("download errors found for {} resources.", errors);
            log.info("total elapsed time: {}", elapsedTime.getDurationString());
            log.info(DOWNLOADER_LOG_DELIMITER);
        } else {
            log.info(DOWNLOADER_LOG_DELIMITER);
            log.info("download completed.");
            log.info("elapsed time: {}", elapsedTime.getDurationString());
            log.info(DOWNLOADER_LOG_DELIMITER);
        }

        if (errors > 0) handleError();
    }

    @Override
    public boolean execute() {
        log.info("Request received. Ident: \"{}\", attempts left: {}", msgData.getIdent(), msgData.getAttemptsLeft());
        source = ImportSource.findImportSourceByName(msgData.getIdent());
        if (source == null) {
            log.error("Source {} not found.", msgData.getIdent());
        } else {
            sourceInfo = source.getSourceInfo();
            pipelineInfo = source.getPipelineInfo();
            DownloaderTaskHandler handler;

            if (sourceInfo == null || !sourceInfo.isObject() || !sourceInfo.hasNonNull(HANDLER)) {
                log.warn("Source: ({}) source_info is invalid {}.", msgData.getIdent(), sourceInfo);
            } else {
                String handlerName = sourceInfo.get(HANDLER).asText();
                handler = receiver.getDownloaderHandlerFactory().getHandler(handlerName);
                if (handler == null) {
                    log.warn("Source: ({}) source_info handler not found {}", msgData.getIdent(), handlerName);
                } else if (pipelineInfo == null || !pipelineInfo.isObject()) {
                    log.warn("Source: ({}) pipeline_info is null or not an object.", msgData.getIdent());
                } else {
                    doExecute(handler);
                }
            }
        }
        return true;
    }

    @Override
    public long getTag() {
        return tag;
    }

    @Override
    public void setTag(long tag) {
        this.tag = tag;
    }
}
