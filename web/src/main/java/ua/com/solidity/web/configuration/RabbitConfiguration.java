package ua.com.solidity.web.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Configuration
@PropertySource({
        "classpath:web.properties",
        "classpath:application.properties"
})
@RequiredArgsConstructor
public class RabbitConfiguration {

    @Value("${spring.rabbitmq.host}")
    private String queueHost;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${scheduler.rabbitmq.name}")
    private String schedulerQueue;
    @Value("${downloader.rabbitmq.name}")
    private String downloaderQueue;
    @Value("${dwh.rabbitmq.name}")
    private String dwhQueue;
    @Value("${report.rabbitmq.name}")
    private String reportQueue;
    @Value("${importer.rabbitmq.name}")
    private String importerQueue;
    @Value("${notification.rabbitmq.name}")
    private String notificationQueue;
    @Value("${statuslogger.rabbitmq.name}")
    private String statusloggerQueue;

    @Bean(name = "schedulerQueue")
    public Queue schedulerQueue() {
        return new Queue(schedulerQueue);
    }

    @Bean(name = "downloaderQueue")
    public Queue downloaderQueue() {
        return new Queue(downloaderQueue);
    }

    @Bean(name = "dwhQueue")
    public Queue dwhQueue() {
        return new Queue(dwhQueue);
    }

    @Bean(name = "reportQueue")
    public Queue reportQueue() {
        return new Queue(reportQueue);
    }

    @Bean(name = "importerQueue")
    public Queue importerQueue() {
        return new Queue(importerQueue);
    }

    @Bean(name = "notificationQueue")
    public Queue notificationQueue() {
        return new Queue(notificationQueue);
    }

    @Bean(name = "statusloggerQueue")
    public Queue statusloggerQueue() {
        return new Queue(statusloggerQueue);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(queueHost);
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        return cachingConnectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }
}
