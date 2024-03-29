package ua.com.solidity.importer;

import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.DurationPrinter;
import ua.com.solidity.common.ImporterMessageData;
import ua.com.solidity.common.OutputStats;
import ua.com.solidity.common.prototypes.PPCustomDBWriter;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.pipeline.Pipeline;
import ua.com.solidity.pipeline.PipelineFactory;


@CustomLog
@Component
public class Importer {
    private final PipelineFactory importerFactory;
    private final Config config;

    @Autowired
    public Importer(PipelineFactory importerFactory, Config config) {
        this.importerFactory = importerFactory;
        this.config = config;
    }

    public final Config getConfig() {
        return this.config;
    }

    public boolean doImport(ImporterMessageData data) {
        DurationPrinter elapsedTime = new DurationPrinter();
        Pipeline pipeline = importerFactory.createPipelineByNode(data.getPipelineInfo());
        if (pipeline == null || !pipeline.isValid()) {
            log.error("Pipeline is invalid.");
            return false;
        }
        ImportSource source = ImportSource.findImportSourceById(data.getImportSourceId());
        pipeline.setParam("source", source != null ? source.getName() : "unknown");
        pipeline.setParam("data", data);
        pipeline.setParam("FileName", data.getData().getMainFile().getFileName());
        pipeline.setParam("OutputFolder", config.getImporterOutputFolder());
        pipeline.setParam("logger", data.createLogger());
        log.info("Import started");
        boolean res;
        try {
            res = pipeline.execute();
            if (!res) {
                log.error("Pipeline execution failed.");
                return false;
            } else {
                log.info("Pipeline execution completed.");
            }
        } catch (Exception e) {
            // can re-raise exception???
            log.error("Error due pipeline execution.", e);
            return false;
        }

        elapsedTime.stop();

        OutputStats stats = pipeline.getParam(PPCustomDBWriter.OUTPUT_STATS, OutputStats.class);

        if (stats != null) {
            for (OutputStats.Group group : stats.items.values()) {
                log.info("Group imported. " + group.getStatsMessage());
            }
            log.info("Import completed. {} / {}", stats.source, elapsedTime.getDurationString());
        } else {
            log.info("All import tasks completed. {}", elapsedTime.getDurationString());
        }
        return true;
    }
}
