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

    private static final String TEST_PIPELINE = ("{'pipeline': [" +
            "{'prototype' : 'InputStream', 'name' : 'stream'}," +
            "{'prototype': 'CSVParser', 'name': 'csvParser', 'inputs': {'stream' : 'stream'}, 'data': {'parseFieldNames' : true, 'quote': '\\\"', 'encoding': 'UTF-8', 'delimiter' : ';', 'splitMode' : true, 'ignoreCharsNearDelimiter': '\\b\\r\\f\\t '}}," +
            "{'prototype' : 'TmpSourceImporter', 'name' : 'handler', 'inputs': {'input': 'csvParser'}, 'data': {'group': 'tmpSource'}}" +
            "]}").replace("'", "\"");

    private final PipelineFactory importerFactory;
    private final ModelRepository repository;

    @Autowired
    public Importer(PipelineFactory importerFactory, ModelRepository repository) {
        this.importerFactory = importerFactory;
        this.repository = repository;
    }
    public void doImport(ImporterMessageData data) {
        DurationPrinter elapsedTime = new DurationPrinter();
        Pipeline pipeline = importerFactory.createPipeline(TEST_PIPELINE);
        if (pipeline == null || !pipeline.isValid()) {
            log.error("Pipeline is invalid.");
            return;
        }
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
                log.info("  DB insert errors: {}", group.getInsertErrorCount());
                log.info("  DB insert error info errors: {}", group.getInsertErrorInfoCount());
                log.info(LOG_INTERNAL_DELIMITER);
                log.info("Inserted: {} ({}%)", group.getInsertCount(), String.format("%.03f", group.getHandledPercent()));
                log.info("Errors handled: {} ({}%)", group.getParseErrorCount(), String.format("%.03f", group.getErrorHandledPercent()));
                log.info(LOG_DELIMITER);
            }
        }

        log.info("Elapsed time: {}", elapsedTime.getDurationString());
        log.info("Import completed.");
    }
}
