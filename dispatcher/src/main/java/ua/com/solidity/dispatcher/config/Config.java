package ua.com.solidity.dispatcher.config;

import java.util.UUID;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:dispatcher.properties"})
public class Config {
    public static final UUID id = UUID.randomUUID();
}
