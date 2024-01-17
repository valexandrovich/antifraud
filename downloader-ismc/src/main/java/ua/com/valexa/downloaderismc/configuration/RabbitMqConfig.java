package ua.com.valexa.downloaderismc.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue requestQueue() {
        return new Queue("ismc-downloader", true);
    }

    @Bean
    public Queue manageQueue() {
        return new Queue("ismc-downloader-manage", true);
    }



}
