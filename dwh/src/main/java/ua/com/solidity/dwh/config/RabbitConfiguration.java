package ua.com.solidity.dwh.config;

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
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.dwh.listener.Receiver;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitConfiguration {

    @Value("${dwh.rabbitmq.name}")
    private String dwhQueue;
    @Value("${spring.rabbitmq.host}")
    private String queueHost;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;

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

    @Bean(name = "dwhQueue")
    public Queue dwhQueue() {
        return new Queue(dwhQueue);
    }

    @Bean
    RabbitMQListener listener(Receiver receiver) {
        return new RabbitMQListener(receiver, dwhQueue);
    }
}
