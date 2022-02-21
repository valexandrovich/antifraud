package ua.com.solidity.dwh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.Utils;

@Slf4j
@SpringBootApplication
public class DwhApplication {
    private final RabbitMQListener listener;

    @Autowired
    public DwhApplication(RabbitMQListener listener, ApplicationContext context) {
        this.listener = listener;
        Utils.setApplicationContext(context);
    }

    public static void main(String[] args) {
        SpringApplication.run(DwhApplication.class, args).getBean(DwhApplication.class).run();
    }

    public final void run() {
        this.listener.start();
        log.info("=== DWH integration started and waits for messages ===");
    }

}
