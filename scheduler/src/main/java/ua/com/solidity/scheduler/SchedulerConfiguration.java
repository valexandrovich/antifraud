package ua.com.solidity.scheduler;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfiguration {
    @Bean
    Queue queue(Config config) {
        return new Queue(config.getQueueName(), false);
    }

    @Bean
    TopicExchange exchange(Config config) {
        return new TopicExchange(config.getTopicExchangeName());
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange, Config config) {
        return BindingBuilder.bind(queue).to(exchange).with(config.getRoutingKey());
    }

    @Bean
    SimpleMessageListenerContainer container(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter, Config config) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(config.getQueueName());
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    Scheduler mainScheduler(RabbitTemplate rabbitTemplate) {
        return new Scheduler(rabbitTemplate);
    }
}
