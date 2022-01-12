package ua.com.solidity.notification.config;

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
import ua.com.solidity.notification.listener.Receiver;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitConfiguration {

    @Value("${notification.rabbitmq.name}")
    private String queueName;
    @Value("${spring.rabbitmq.host}")
    private String queueHost;

    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(queueHost);
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public Queue myQueue() {
        return new Queue(queueName);
    }

    @Bean
    RabbitMQListener listener(Receiver receiver) {
        return new RabbitMQListener(receiver, queueName);
    }

}
