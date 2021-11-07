package ua.com.solidity.downloader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ua.com.solidity.common.Utils;

@Slf4j
@SpringBootApplication
public class DownloaderApp {
    private final Config config;
    private final ConnectionFactory connectionFactory;
    private final Receiver receiver;

    @Autowired
    public DownloaderApp(Config config, ConnectionFactory connectionFactory, Receiver receiver) {
        this.config = config;
        this.connectionFactory = connectionFactory;
        this.receiver = receiver;
    }

    public final void run() {
        Utils.startRabbitMQContainer(connectionFactory, config.getQueueName(), receiver);
        log.info("output folder: {}", config.getDownloaderOutputFolder());
        log.info("=== Downloader started and wait for messages ===");
    }

    public static void main(String[] args) {
        SpringApplication.run(DownloaderApp.class, args).getBean(DownloaderApp.class).run();
    }
}
