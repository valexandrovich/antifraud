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
        return new CSVParams(false, "UTF-8", true, ";", "\"", "\b\r\f\t ", false, true);
    }
}
