package ua.com.solidity.dwh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Slf4j
@SpringBootApplication
@EntityScan(basePackages = {"ua.com.solidity.db.entities", "ua.com.solidity.dwh.entities"})
public class DwhApplication {

	public static void main(String[] args) {
		SpringApplication.run(DwhApplication.class, args).getBean(DwhApplication.class).run();
	}

	public final void run() {
		log.info("=== DWH integration started and waits for messages ===");
	}

}
