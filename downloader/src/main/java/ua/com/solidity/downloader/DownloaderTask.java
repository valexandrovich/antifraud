package ua.com.solidity.downloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.*;
import ua.com.solidity.db.entities.ImportRevision;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.repositories.ImportRevisionRepository;

import java.util.UUID;

@Getter
@Setter
@Slf4j
public class DownloaderTask extends RabbitMQTask {
    private static final String DOWNLOADER_LOG_DELIMITER = "- - - - - - - - - - - - -";
    private static final String HANDLER = "handler";
    private Receiver receiver;
    private DownloaderMessageData msgData;
    private JsonNode pipelineInfo;
    private JsonNode sourceInfo;
    private ImportSource source;
    private boolean isError = false;
    private final ImportRevisionRepository importRevisionRepository;

    protected DownloaderTask(Receiver receiver, DownloaderMessageData msgData) {
        super(true);
        this.receiver = receiver;
        this.msgData = msgData;
        this.importRevisionRepository = Utils.checkApplicationContext() ?
                Utils.getApplicationContext().getBean(ImportRevisionRepository.class) : null;
    }

    public final String getPrefix() {
        return source == null ? "" : source.getName() + "-";
    }

    public void handleError() {
        isError = true;
        if (msgData.decrementAttemptsLeft()) {
            if (msgData.getDelayMinutes() <= 0) {
                Utils.sendRabbitMQMessage(receiver.getConfig().getName(), Utils.objectToJsonString(msgData));
            } else {
                ObjectNode node = Utils.getSortedMapper().createObjectNode();
                node.set("action", JsonNodeFactory.instance.textNode("exec"));
                node.set("exchange", JsonNodeFactory.instance.textNode(receiver.getConfig().getName()));
                node.set("sleep_ms", JsonNodeFactory.instance.numberNode(msgData.getDelayMinutes() * 60000));
                node.set("data", Utils.getJsonNode(msgData));
                Utils.sendRabbitMQMessage(receiver.getConfig().getSchedulerTopicExchangeName(), Utils.objectToJsonString(node));
            }
        } else {
            DownloaderErrorMessageData data = new DownloaderErrorMessageData(this.msgData.getIdent());
            RabbitMQLogMessage message = new RabbitMQLogMessage("downloader", "E001", data);
            Utils.sendRabbitMQMessage(receiver.getConfig().getLogExchangeName(), Utils.objectToJsonString(message));
        }
    }

    public final ImporterMessageData createImporterMessageData(ResourceInfoData data) {
        ImportRevision res = new ImportRevision(UUID.randomUUID(), source.getId(), data.getMainFile().getInstant(),
                pipelineInfo, data.getMainFile().getUrl(), data.getMainFile().getFileName());
        res = res.save();
        if (res != null) {
            String mailTo = msgData.getLogMailTo();
            if (mailTo == null || mailTo.isBlank()) {
                mailTo = receiver.getConfig().getDefaultMailTo();
            }
            long limit = msgData.getLogLimit();
            if (limit < 0) {
                limit = receiver.getConfig().getDefaultLogLimit();
            }
            String logFileName = receiver.getConfig().getLogFileName(msgData.getLogFile());
            return new ImporterMessageData(source.getId(), res.getId(), data, pipelineInfo, logFileName, mailTo, limit);
        }
        return null;
    }

    @Override
    public DeferredAction compareWith(DeferredTask task) {
        DownloaderTask otherTask = (DownloaderTask) task;
        return otherTask != null && otherTask.msgData.getIdent().equals(msgData.getIdent()) ? DeferredAction.IGNORE : DeferredAction.APPEND;
    }

    private void doExecuteHandlerInfo(ResourceInfoData data, DurationPrinter urlElapsedTime) {
        urlElapsedTime.reset();
        ImporterMessageData importerMessageData = receiver.getDownloader().download(data, this);
        urlElapsedTime.stop();
        if (importerMessageData != null) {
            importerMessageData.setPipelineInfo(pipelineInfo);
            send(receiver.getConfig().getImporterTopicExchangeName(), Utils.objectToJsonString(importerMessageData));
            log.info("{} files found/ downloaded, message sent to Importer ({}).", data.dictionaries.size() + 1, urlElapsedTime.getDurationString());
        } else {
            handleError();
            log.error("Downloading cancelled. All downloaded files removed.");
        }
    }

    private void doExecute(DownloaderTaskHandler handler) {
        ImportRevision lastRevision = ImportRevision.findFirstBySourceName(source.getId());
        ResourceInfoData data = handler.getData(this);
        if (isError) return;
        if (lastRevision != null && lastRevision.getRevisionDate() != null &&
                data.getMainFile().getRevisionDateTime().toInstant().compareTo(lastRevision.getRevisionDate()) <= 0) {
            log.info("New files not found for source name = {}.", source.getName());
            return;
        }
        DurationPrinter elapsedTime = new DurationPrinter();
        doExecuteHandlerInfo(data, elapsedTime);
    }

    @Override
    protected void execute() {
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
    }
}
