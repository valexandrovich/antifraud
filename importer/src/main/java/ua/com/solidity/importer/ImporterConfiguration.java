package ua.com.solidity.importer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.solidity.common.ActionObject;
import ua.com.solidity.common.OtpExchange;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.parsers.csv.CSVParams;
import ua.com.solidity.common.stats.StatsAction;
import ua.com.solidity.db.entities.ImportSource;

import javax.annotation.PostConstruct;

@Configuration
public class ImporterConfiguration {
    @Bean
    RabbitMQListener listener(Receiver receiver) {
        return new RabbitMQListener(receiver, OtpExchange.IMPORTER);
    }
    @Bean
    CSVParams defaultParams() {
        return new CSVParams("UTF-8", ";", "\"", null, "\b\r\f\t ",CSVParams.FLAG_PARSE_FIELD_NAMES | CSVParams.FLAG_AUTO_TRIM, -1);
    }

    private ArrayNode getPipelineBy(Object value) {
        ImportSource source = null;
        if (value instanceof String) {
            source = ImportSource.findImportSourceByName((String) value);
        } else if (value instanceof Integer) {
            source = ImportSource.findImportSourceById((Integer) value);
        }
        if (source == null) return null;
        JsonNode pipelineInfo = source.getPipelineInfo();
        if (pipelineInfo == null || !pipelineInfo.isObject() || !pipelineInfo.has("pipeline")) return null;
        JsonNode pipeline = pipelineInfo.get("pipeline");
        return pipeline.isArray() ? (ArrayNode) pipeline : null;
    }

    @PostConstruct
    void initRabbitMQActions() {
        StatsAction.setPipelineGetter(this::getPipelineBy);
        ActionObject.register(StatsAction.class, "stats");
    }
}
