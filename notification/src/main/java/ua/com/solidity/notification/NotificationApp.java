package ua.com.solidity.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.Utils;

@Slf4j
@SpringBootApplication
public class NotificationApp {

    private final RabbitMQListener listener;

    @Autowired
    public NotificationApp(RabbitMQListener listener, ApplicationContext context) {
        this.listener = listener;
        Utils.setApplicationContext(context);
    }

    public final void run() {
        this.listener.start();
        log.info("=== Notification started and waits for messages ===");
    }

    public static void main(String[] args) {
        SpringApplication.run(NotificationApp.class, args).getBean(NotificationApp.class).run();
    }

}
