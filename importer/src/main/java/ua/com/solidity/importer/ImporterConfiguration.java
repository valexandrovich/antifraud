package ua.com.solidity.importer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.solidity.common.ActionObject;
import ua.com.solidity.common.OtpExchange;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.parsers.csv.CSVParams;
import ua.com.solidity.common.stats.StatsAction;

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
    @PostConstruct
    void initRabbitMQActions() {
        ActionObject.register(StatsAction.class, "stats");
    }
}
