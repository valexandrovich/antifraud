package ua.com.solidity.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.solidity.common.RabbitMQListener;

@Configuration
public class SchedulerConfiguration {
    @Bean
    RabbitMQListener listener(Config config, Receiver receiver) {
        return new RabbitMQListener(receiver, config.getName());
    }

    @Bean
    Scheduler mainScheduler() {
        return new Scheduler();
    }
}
