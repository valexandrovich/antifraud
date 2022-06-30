package ua.com.solidity.scheduler;

import lombok.CustomLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.FileCopyUtils;
import ua.com.solidity.common.RabbitMQListener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@CustomLog
@Configuration
@PropertySource({"classpath:scheduler.properties", "classpath:application.properties"})
public class SchedulerConfiguration {
    @Bean
    RabbitMQListener listener(Config config, Receiver receiver) {
        return new RabbitMQListener(receiver, config.getName());
    }

    @Bean
    Scheduler mainScheduler() {
        return new Scheduler();
    }

    @Bean
    String schedulerInitTasks(Config config) {
        try (InputStream stream = getClass().getResourceAsStream("/" + config.getSchedulerInitFile())) {
            assert stream != null;
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            log.error("Error reading scheduler init message from file {}", config.getSchedulerInitFile(), e);
        }
        return "{\"action\":\"update\", \"data\":[]}";
    }
}
