package ua.com.solidity.downloader;

import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ua.com.solidity.common.RabbitMQListener;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.logger.LoggerWrapperFactory;


@CustomLog
@EnableJpaRepositories(basePackages = "ua.com.solidity.db.repositories")
@EntityScan("ua.com.solidity.db.entities")
@SpringBootApplication
public class DownloaderApp {
    private final RabbitMQListener listener;

    @Autowired
    public DownloaderApp(RabbitMQListener listener, ApplicationContext context) {
        this.listener = listener;
        Utils.setApplicationContext(context);
    }

    public final void run() {
        this.listener.start();
        log.info("=== Downloader started and waits for messages ===");
    }

    public static void main(String[] args) {
        SpringApplication.run(DownloaderApp.class, args).getBean(DownloaderApp.class).run();
    }
}
