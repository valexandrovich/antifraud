package ua.com.solidity.importer;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImporterConfiguration {
    @Bean
    Queue queue(ua.com.solidity.importer.Config config) {
        return new Queue(config.getQueueName(), false);
    }

    @Bean
    TopicExchange exchange(ua.com.solidity.importer.Config config) {
        return new TopicExchange(config.getTopicExchangeName());
    }

    @Bean
    Binding binding(ua.com.solidity.importer.Config config, Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(config.getRoutingKey());
    }
    @Bean
    ua.com.solidity.importer.CSVParams defaultParams() {
        return new ua.com.solidity.importer.CSVParams("UTF-8", false, true,";", "\"", "\b\r\f\t ");
    }
}
