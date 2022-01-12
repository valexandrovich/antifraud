package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileCopyUtils;
import ua.com.solidity.common.RabbitMQListener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class SchedulerTestConfiguration {
    @Bean
    RabbitMQListener listener(Config config, Receiver receiver) {
        return new RabbitMQListener(receiver, config.getCollectMSecs(), config.getName(), config.getTest());
    }

    @Bean
    String schedulerInitMessage(Config config) {
        try(InputStream stream = getClass().getResourceAsStream("/"+config.getSchedulerInitFile())) {
            assert stream != null;
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            return FileCopyUtils.copyToString(reader);
        } catch(Exception e) {
            log.error("Error reading scheduler init message from file {}", config.getSchedulerInitFile());
        }
        return "{\"action\":\"update\", \"data\":[]}";
    }
}
