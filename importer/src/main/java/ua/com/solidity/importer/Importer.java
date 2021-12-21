package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.DurationPrinter;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.OutputStats;
import ua.com.solidity.common.prototypes.PPCustomDBWriter;
import ua.com.solidity.pipeline.Pipeline;
import ua.com.solidity.pipeline.PipelineFactory;

@Slf4j
@Component
public class Importer {
    private static final String LOG_DELIMITER = "-------------------------------------------------------";
    private static final String LOG_INTERNAL_DELIMITER = "- - - - - - - - - - - - - - - - - -";
    private static final String PERCENT_FIELD = "%.03f";

    private final PipelineFactory importerFactory;
    private final ModelRepository repository;

    @Autowired
    public Importer(PipelineFactory importerFactory, ModelRepository repository) {
        this.importerFactory = importerFactory;
        this.repository = repository;
    }

    public void doImport(ImporterMessageData data) {
        DurationPrinter elapsedTime = new DurationPrinter();
        Pipeline pipeline = importerFactory.createPipelineByNode(data.getPipelineInfo());
        if (pipeline == null || !pipeline.isValid()) {
            log.error("Pipeline is invalid.");
            return;
        }
        pipeline.setParam("data", data);
        pipeline.setParam("FileName", data.getDataFileName());
        pipeline.setParam("repository", repository);
        log.info(LOG_DELIMITER);
        log.info("Import started");
        log.info(LOG_DELIMITER);
        try {
            boolean res = pipeline.execute();
            if (!res) {
                log.error("Pipeline execution failed.");
            } else {
                log.info("Pipeline execution completed.");
            }
            log.info(LOG_DELIMITER);
        } catch (Exception e) {
            log.error("Error due pipeline execution.", e);
        }

        elapsedTime.stop();

        OutputStats stats = pipeline.getParam(PPCustomDBWriter.OUTPUT_STATS, OutputStats.class);

        if (stats != null) {
            for (OutputStats.Group group : stats.items.values()) {
                log.info("target group: {}", group.getName());
                log.info(LOG_DELIMITER);
                log.info("  Total rows: {}", group.getTotalRowCount());
                log.info("  Parse errors: {}", group.getParseErrorCount());
                log.info("  Rows inserted: {}", group.getInsertCount());
                log.info("  Rows ignored: {}", group.getInsertIgnoreCount());
                log.info(LOG_INTERNAL_DELIMITER);
                log.info("  Insert errors: {}", group.getInsertErrorCount());
                log.info("  Errors on error publication: {}", group.getInsertErrorInfoCount());
                log.info(LOG_INTERNAL_DELIMITER);
                log.info("Inserted: {} ({}%)", group.getInsertCount(), String.format(PERCENT_FIELD, group.getInsertedPercent()));
                log.info("Ignored: {} ({}%)", group.getInsertIgnoreCount(), String.format(PERCENT_FIELD, group.getIgnoredPercent()));
                log.info("Errors handled: {} ({}%)", group.getParseErrorCount(), String.format(PERCENT_FIELD, group.getErrorHandledPercent()));
                log.info("Errors not handled: {} ({}%)", group.getInsertErrorInfoCount(), String.format(PERCENT_FIELD, group.getErrorNotHandledPercent()));
                log.info(LOG_INTERNAL_DELIMITER);
                log.info("Rows handled: {}/{} ({}%)", group.getInsertCount() + group.getInsertIgnoreCount(), group.getTotalRowCount(), String.format(PERCENT_FIELD, group.getHandledPercent()));
                log.info(LOG_DELIMITER);
            }
        }

        log.info("Elapsed time: {}", elapsedTime.getDurationString());
        log.info("Import completed.");
    }
}
