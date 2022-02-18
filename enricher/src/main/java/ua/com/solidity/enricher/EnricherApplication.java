package ua.com.solidity.enricher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.Utils;

@Slf4j
@SpringBootApplication
public class EnricherApplication {
    private final RabbitMQListener listener;

    @Autowired
    public EnricherApplication(RabbitMQListener listener, ApplicationContext context) {
        this.listener = listener;
        Utils.setApplicationContext(context);
    }

    public static void main(String[] args) {
        SpringApplication.run(EnricherApplication.class, args).getBean(EnricherApplication.class).run();
    }

    public final void run() {
        this.listener.start();
        log.info("=== Enricher started and waits for messages ===");
    }

}
