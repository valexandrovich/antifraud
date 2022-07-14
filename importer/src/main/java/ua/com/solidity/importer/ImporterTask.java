package ua.com.solidity.importer;

import lombok.CustomLog;
import ua.com.solidity.common.*;
import ua.com.solidity.db.entities.ImportRevision;
import ua.com.solidity.db.entities.ImportSource;


@CustomLog
public class ImporterTask extends RabbitMQTask {
    private static final String DROP_REVISION = "drop_revision";
    private static final String REMOVE_FILES = "removeFiles";
    private final Importer importer;
    private final ImporterMessageData data;
    private String importSourceName;
    protected ImporterTask(Importer importer, ImporterMessageData data) {
        super();
        this.importer = importer;
        this.data = data;
    }

    @Override
    protected DeferredAction compareWith(DeferrableTask task) {
        return DeferredAction.APPEND;
    }

    @Override
    public boolean isDeferred() {
        return false;
    }

    @Override
    protected void rmqExecute() {
        ImportSource importSource = ImportSource.findImportSourceById(data.getImportSourceId());

        if (importSource == null) {
            log.error("Import source not found for id={}.", data.getImportSourceId());
            return;
        }

        importSourceName = importSource.getName();

        if (!ImportSource.sourceLocker(data.getImportSourceId(), true)) {
            log.info("=== Import source (id: {}) already locked in another session. ===", data.getImportSourceId());
            enqueueBack();
            return;
        }
        try {
            log.info("File import requested: {}.", data.getData().getMainFile().getFileName());
            importer.doImport(data);
            if (data.getExtraData(DROP_REVISION).asBoolean(false)) { // god mode
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
        log.info("=== Import source (id: {}, \"{}\") unlocked === ", data.getImportSourceId(), importSource.getName());
    }

    @Override
    protected String description() {
        return Utils.messageFormat("(source: {}, revision: {})", importSourceName == null ? "<undefined>" : importSourceName, data.getImportRevisionId());
    }
}
