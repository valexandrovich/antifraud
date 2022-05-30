package ua.com.solidity.importer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.parsers.csv.CSVParams;

@Configuration
public class ImporterConfiguration {
    @Bean
    RabbitMQListener listener(Config config, Receiver receiver) {
        return new RabbitMQListener(receiver, config.getQueueName());
    }
    @Bean
    CSVParams defaultParams() {
        return new CSVParams("UTF-8", ";", "\"", null, "\b\r\f\t ",CSVParams.FLAG_PARSE_FIELD_NAMES | CSVParams.FLAG_AUTO_TRIM, -1);
    }
}
