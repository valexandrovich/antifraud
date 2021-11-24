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
    Queue queue(Config config) {
        return new Queue(config.getQueueName(), false);
    }

    @Bean
    TopicExchange exchange(Config config) {
        return new TopicExchange(config.getTopicExchangeName());
    }

    @Bean
    Binding binding(Config config, Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(config.getRoutingKey());
    }
}
