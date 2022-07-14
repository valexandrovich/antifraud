package ua.com.solidity.enricher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ua.com.solidity.common.Utils;

@Slf4j
@SpringBootApplication
@EntityScan(basePackages = {"ua.com.solidity.db.entities"})
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories", "ua.com.solidity.enricher.repository"})
@PropertySource({"classpath:enricher.properties", "classpath:application.properties"})
public class EnricherApplication {

    @Autowired
    public EnricherApplication(ApplicationContext context) {
        Utils.setApplicationContext(context);
    }

    public static void main(String[] args) {
        SpringApplication.run(EnricherApplication.class, args).getBean(EnricherApplication.class).run();
    }

    public final void run() {
        log.info("=== Enricher started and waits for messages ===");
    }

}
