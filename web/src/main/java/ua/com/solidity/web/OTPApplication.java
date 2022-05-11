package ua.com.solidity.web;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import ua.com.solidity.web.configuration.SwaggerConfiguration;


@EnableSwagger2
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories"})
@EntityScan(basePackages = {"ua.com.solidity.db.entities"})
@SpringBootApplication
@Import({SwaggerConfiguration.class})
public class OTPApplication {

	public static void main(String[] args) {
		SpringApplication.run(OTPApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
