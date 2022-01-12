package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.*;

@Slf4j
public class ImporterTask extends RabbitMQTask {
    private final Importer importer;
    private final ImporterMessageData data;
    protected ImporterTask(Importer importer, ImporterMessageData data) {
        super(true);
        this.importer = importer;
        this.data = data;
    }

    @Override
    protected DeferredAction compareWith(DeferredTask task) {
        return DeferredAction.APPEND;
    }

    @Override
    protected void execute() {
        log.info("File import requested: {}.", data.getData().getMainFile().getFileName());
        importer.doImport(data);
        data.getData().removeAllFiles();
    }
}
