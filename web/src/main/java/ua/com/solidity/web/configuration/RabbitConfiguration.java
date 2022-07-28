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
import ua.com.solidity.common.OtpExchange;

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

    private static final String MAX_PRIORITY = "x-max-priority";

    @Bean(name = "schedulerQueue")
    public Queue schedulerQueue() {
        return new Queue(OtpExchange.SCHEDULER);
    }

    @Bean(name = "downloaderQueue")
    public Queue downloaderQueue() {
        return new Queue(OtpExchange.DOWNLOADER);
    }

    @Bean(name = "dwhQueue")
    public Queue dwhQueue() {
        return new Queue(OtpExchange.DWH);
    }

    @Bean(name = "reportQueue")
    public Queue reportQueue() {
        return new Queue(OtpExchange.REPORT);
    }

    @Bean(name = "importerQueue")
    public Queue importerQueue() {
        return new Queue(OtpExchange.IMPORTER);
    }

    @Bean(name = "notificationQueue")
    public Queue notificationQueue() {
        return new Queue(OtpExchange.NOTIFICATION);
    }

    @Bean(name = "statusloggerQueue")
    public Queue statusloggerQueue() {
        return new Queue(OtpExchange.STATUS_LOGGER);
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
