package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import ua.com.solidity.common.*;
import ua.com.solidity.db.entities.ImportRevision;
import ua.com.solidity.db.entities.ImportSource;

@Slf4j
public class ImporterTask extends RabbitMQTask {
    private static final String DROP_REVISION = "drop_revision";
    private static final String REMOVE_FILES = "removeFiles";
    private final Importer importer;
    private final ImporterMessageData data;
    protected ImporterTask(Importer importer, ImporterMessageData data) {
        super(false);
        this.importer = importer;
        this.data = data;
    }

    @Override
    protected DeferredAction compareWith(DeferredTask task) {
        return DeferredAction.APPEND;
    }

    @Override
    protected void execute() {
        if (!ImportSource.sourceLocker(data.getImportSourceId(), true)) {
            log.info("Import source (id: {}) already locked in another session.", data.getImportSourceId());
            acknowledge(false);
            return;
        }
        try {
            acknowledge(true);
            log.info("File import requested: {}.", data.getData().getMainFile().getFileName());
            importer.doImport(data);
            if (data.getExtraData(DROP_REVISION).asBoolean(false)) {
                ImportRevision.removeRevision(data.getImportRevisionId());
                log.info("--- revision and revision_group rows removed ---");
            }
            if (data.getExtraData(REMOVE_FILES).asBoolean(importer.getConfig().isRemoveFiles())) {
                data.getData().removeAllFiles();
                log.info("--- all files removed ---");
            }

        } finally {
            ImportSource.sourceLocker(data.getImportSourceId(), false);
        }
        log.info("== Import source (id: {}) unlocked === ", data.getImportSourceId());
    }
}
