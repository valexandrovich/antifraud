package ua.com.solidity.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.Utils;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories(basePackages = "ua.com.solidity.db.repositories")
@EntityScan("ua.com.solidity.db.entities")
public class SchedulerApp {
    private final RabbitMQListener listener;
    private final Scheduler mainScheduler;

    public SchedulerApp(RabbitMQListener listener, Scheduler mainScheduler, ApplicationContext context) {
        this.listener = listener;
        this.mainScheduler = mainScheduler;
        Utils.setApplicationContext(context);
    }

    public final void run() {
        listener.start();
        mainScheduler.refresh();
        log.info("=== Scheduler started ===");
    }

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApp.class, args).getBean(SchedulerApp.class).run();
    }
}
