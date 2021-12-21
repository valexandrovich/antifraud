package ua.com.solidity.schedulertest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@NoArgsConstructor
public class Config {
    @Value("${scheduler.rabbitmq.name}")
    private String name;
    @Value("${scheduler.rabbitmq.scheduler}")
    private String scheduler;
    @Value("${scheduler.rabbitmq.test}")
    private String test;
    @Value("${scheduler.schedulerInitFile}")
    private String schedulerInitFile;
    @Value("${scheduler.rabbitmq.collectMSecs}")
    private int collectMSecs;
}
