package ua.com.solidity.importer;

import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.Utils;


@CustomLog
@EnableJpaRepositories(basePackages = "ua.com.solidity.db.repositories")
@EntityScan("ua.com.solidity.db.entities")
@SpringBootApplication
public class ImporterApp {
    private final RabbitMQListener listener;
    @Autowired
    public ImporterApp(RabbitMQListener listener, ApplicationContext context) {
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
