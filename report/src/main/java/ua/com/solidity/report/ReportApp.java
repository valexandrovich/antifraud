package ua.com.solidity.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ReportApp {

	public final void run() {
		log.info("=== Report started and waits for messages ===");
	}

	public static void main(String[] args) {
		SpringApplication.run(ReportApp.class, args).getBean(ReportApp.class).run();
	}
}
