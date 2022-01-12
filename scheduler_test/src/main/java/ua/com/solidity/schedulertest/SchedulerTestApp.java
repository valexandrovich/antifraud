package ua.com.solidity.schedulertest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.Utils;

@Slf4j
@SpringBootApplication
public class SchedulerTestApp {
    private final RabbitMQListener listener;

    @Autowired
    public SchedulerTestApp(RabbitMQListener listener, ApplicationContext context) {
        this.listener = listener;
        Utils.setApplicationContext(context);
    }

    public final void run() {
        this.listener.start();
        log.info("=== Scheduler TEST started. ===");
    }

    public static void main(String[] args) {
        SpringApplication.run(SchedulerTestApp.class, args).getBean(SchedulerTestApp.class).run();
    }
}
