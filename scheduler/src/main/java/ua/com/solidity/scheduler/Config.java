package ua.com.solidity.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class Config {
    @Value("${scheduler.rabbitmq.name}")
    private String name;
    @Value("${scheduler.rabbitmq.init}")
    private String initName;
}
