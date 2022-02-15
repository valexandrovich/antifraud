package ua.com.solidity.importer;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.Utils;

@Slf4j
@EnableJpaRepositories(basePackages = "ua.com.solidity.db.repositories")
@EntityScan("ua.com.solidity.db.entities")
@SpringBootApplication
public class ImporterApp {
    private final RabbitMQListener listener;
    @Autowired
    public ImporterApp(RabbitMQListener listener, ApplicationContext context, JdbcTemplate template) {
        this.listener = listener;
        Utils.setApplicationContext(context);
    }

    public void run() {
        listener.start();
        log.info("=== Importer started and waits for a message. ===");
    }

    public static void main(String[] args) {
        SpringApplication.run(ImporterApp.class, args).getBean(ImporterApp.class).run();
    }
}
