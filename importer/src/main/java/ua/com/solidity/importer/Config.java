package ua.com.solidity.importer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
public class Config {
    @Value("${importer.rabbitmq.queue}")
    private String queueName;

    @Value("${reservecopy.rabbitmq.queue}")
    private String reserveCopyQueue;
}
