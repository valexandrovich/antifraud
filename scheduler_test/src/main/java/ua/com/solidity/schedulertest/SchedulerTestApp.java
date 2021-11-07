package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootApplication
public class SchedulerTestApp {
    @Bean
    Queue queue(Config config) {
        return new Queue(config.getQueueNameForInit(), false);
    }

    @Bean
    Queue testQueue(Config config) {
        return new Queue(config.getQueueNameForTest(), false);
    }

    @Bean
    TopicExchange exchange(Config config) {
        return new TopicExchange(config.getTopicExchangeName());
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange, Config config) {
        return BindingBuilder.bind(queue).to(exchange).with(config.getInitRoutingKey());
    }

    @Bean
    Binding testBinding(Queue testQueue, TopicExchange exchange, Config config) {
        return BindingBuilder.bind(testQueue).to(exchange).with(config.getTestRoutingKey());
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter, Config config) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(config.getQueueNameForInit(), config.getQueueNameForTest());
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver, Config config) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "receiveInitMessage");
        adapter.addQueueOrTagToMethodName(config.getQueueNameForTest(), "receiveTestMessage");
        return adapter;
    }

    @Bean
    String schedulerInitMessage(Config config) {
        try(FileInputStream stream = new FileInputStream(ResourceUtils.getFile("classpath:" + config.getSchedulerInitFile()))) {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            return FileCopyUtils.copyToString(reader);
        } catch(Exception e) {
             log.error("Error reading scheduler init message from file {}", config.getSchedulerInitFile());
        }
        return "{\"action\":\"update\", \"data\":[]}";
    }

    public static void main(String[] args) {
        SpringApplication.run(SchedulerTestApp.class, args);
        log.info("=== Scheduler TEST started. ===");
    }
}
