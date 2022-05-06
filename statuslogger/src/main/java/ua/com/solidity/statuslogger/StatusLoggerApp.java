package ua.com.solidity.statuslogger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class StatusLoggerApp {

    public final void run() {
        log.info("=== Statuslogger started and waits for messages ===");
    }

    public static void main(String[] args) {
        SpringApplication.run(StatusLoggerApp.class, args).getBean(StatusLoggerApp.class).run();
    }
}
