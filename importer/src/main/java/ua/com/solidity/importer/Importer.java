package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.DurationPrinter;
import ua.com.solidity.common.ImporterMessageData;

import java.io.FileInputStream;

@Slf4j
@Component
public class Importer {
    private static final String LOG_DELIMITER = "-------------------------------------------------------";
    private static final String LOG_INTERNAL_DELIMITER = "- - - - - - - - - -";
    private final ua.com.solidity.importer.CSVParams params;
    private final ModelRepository repository;

    private long parseErrorsCount;
    private long insertErrorsCount;
    private long totalRowsCount;

    @Autowired
    public Importer(ua.com.solidity.importer.CSVParams defaultParams, ModelRepository repository) {
        this.params = defaultParams;
        this.repository = repository;
    }

    private boolean beginImport(CSVParser parser) {
        if (!parser.open()) return false;
        return repository.truncate();
    }

    private void handleRow(CSVParser parser) {
        ++totalRowsCount;
        if (parser.fields.size() < parser.fieldNames.size()) {
            log.warn("Parse error: {}", parser.lastRow());
            ++parseErrorsCount;
        } else {
            try {
                repository.insertRow(parser);
            } catch (Exception e) {
                log.warn("DB Insertion failed: {}. row: \n{}", e.getMessage(), parser.lastRow());
                ++insertErrorsCount;
            }
        }
    }

    public void doImport(ImporterMessageData data) {
        totalRowsCount = parseErrorsCount = insertErrorsCount = 0;
        log.info(LOG_DELIMITER);
        log.info("Import started");
        log.info(LOG_DELIMITER);

        DurationPrinter elapsedTime = new DurationPrinter();
        try (FileInputStream stream = new FileInputStream(data.getDataFileName())) {
            CSVParser parser = new CSVParser(stream, params);
            if (beginImport(parser)) {
                while (!parser.eof()) {
                    handleRow(parser);
                    parser.next();
                }
                elapsedTime.stop();
                long count = totalRowsCount - parseErrorsCount - insertErrorsCount;
                log.info(LOG_DELIMITER);
                log.info("Import completed.");
                log.info(LOG_INTERNAL_DELIMITER);
                log.info("  Total rows: {}", totalRowsCount);
                log.info("  Parse errors: {}", parseErrorsCount);
                log.info("  DB insert errors: {}", insertErrorsCount);
                log.info(LOG_INTERNAL_DELIMITER);
                log.info("Inserted: {} ({}%)", count, String.format("%.03f", (float)((double)count/totalRowsCount * 100)));
                log.info("Elapsed time: {}", elapsedTime.getDurationString());
                log.info(LOG_DELIMITER);
            } else {
                log.error("CSV-parser not opened properly.");
            }
        } catch(Exception e) {
            log.error("Error while parsing file {}", data.getDataFileName(), e);
         }
    }
}
