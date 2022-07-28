package ua.com.solidity.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DispatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatcherApplication.class, args).getBean(DispatcherApplication.class).run();
    }

    public final void run() {
        log.info("=== Dispatcher started and waits for messages ===");
    }

}
