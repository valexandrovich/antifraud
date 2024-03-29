package ua.com.solidity.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import ua.com.solidity.common.OtpExchange;
import ua.com.solidity.common.Utils;
import ua.com.solidity.web.configuration.SwaggerConfiguration;


@EnableSwagger2
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories", "ua.com.solidity.web.repositories"})
@EntityScan(basePackages = {"ua.com.solidity.db.entities", "ua.com.solidity.web.entities"})
@SpringBootApplication
@Import({SwaggerConfiguration.class})
public class OTPApplication {

    public static void main(String[] args) {
        SpringApplication.run(OTPApplication.class, args);
    }

    @Autowired
    public OTPApplication(ApplicationContext context) {
        Utils.setApplicationContext(context);
        Utils.prepareRabbitMQQueue(OtpExchange.ENRICHER);
    }
}