package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ua.com.solidity.common.Utils;

@Slf4j
@SpringBootApplication
public class ImporterApp {
    private final ua.com.solidity.importer.Config config;
    private final ConnectionFactory connectionFactory;
    private final Receiver receiver;

    @Autowired
    public ImporterApp(Config config, ConnectionFactory connectionFactory, Receiver receiver) {
        this.config = config;
        this.connectionFactory = connectionFactory;
        this.receiver = receiver;
    }

    public void run() {
        Utils.startRabbitMQContainer(connectionFactory, config.getQueueName(), receiver);
        log.info("=== Importer started and wait for a message. ===");
    }

    public static void main(String[] args) {
        SpringApplication.run(ImporterApp.class, args).getBean(ImporterApp.class).run();
    }
}
