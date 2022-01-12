package ua.com.solidity.importer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.OutputCache;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Component
public class Config {
    @Value("${importer.rabbitmq.queue}")
    private String queueName;

    @Value("${reservecopy.rabbitmq.queue}")
    private String reserveCopyQueue;

    @Value("${importer.environmentVariableForImportRestriction}")
    private String environmentVariableForImportRestriction;

    @Value("${importer.importRestriction}")
    private String importRestrictionStr;
    
    private String importerOutputFolder = null;
    private long importRestriction = -1;
    private boolean importRestrictionAssigned = false;

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

    public final boolean canInsertData(OutputCache cache) {
        long restriction = getInputRestriction();
        return restriction < 0 || cache.getGroup().getTotalRowCount() < restriction;
    }
}
