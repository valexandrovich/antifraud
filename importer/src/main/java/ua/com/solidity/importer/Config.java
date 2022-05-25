package ua.com.solidity.importer;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;


@CustomLog
@Getter
@Setter
@Component
public class Config {
    @Value("${importer.rabbitmq.queue}")
    private String queueName;

    @Value("${reservecopy.rabbitmq.queue}")
    private String reserveCopyQueue;

    @Value("${enricher.rabbitmq.name}")
    private String enricherQueueName;

    @Value("${importer.environmentVariableForImportRestriction}")
    private String environmentVariableForImportRestriction;

    @Value("${importer.importRestriction}")
    private String importRestrictionStr;

    @Value("${importer.removeFiles}")
    private boolean removeFiles;
    
    private String importerOutputFolder = null;
    private long importRestriction = -1;
    private boolean importRestrictionAssigned = false;

    @SuppressWarnings("unused")
    public final long getInputRestriction() {
        if (!importRestrictionAssigned) {
            String importRestrictionValue = importRestrictionStr;
            if (importRestrictionValue == null || importRestrictionValue.isBlank()) {
                Map<String, String> map = System.getenv();
                importRestrictionValue = map.getOrDefault(environmentVariableForImportRestriction, null);
            }
            importRestriction = importRestrictionValue != null ? NumberUtils.toLong(importRestrictionValue, -1) : -1;
            importRestrictionAssigned = true;
        }
        return importRestriction;
    }
}
