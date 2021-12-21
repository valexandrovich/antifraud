package ua.com.solidity.otp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.modelmapper.ModelMapper;


@EnableSwagger2
@EnableJpaRepositories(basePackages = "ua.com.solidity.db.repositories")
@EntityScan("ua.com.solidity.db.entities")
@SpringBootApplication
public class OTPApplication {

	public static void main(String[] args) {
		SpringApplication.run(OTPApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
