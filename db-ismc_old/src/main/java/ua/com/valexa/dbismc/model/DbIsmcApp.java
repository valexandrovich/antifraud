package ua.com.valexa.dbismc.model;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EntityScan(basePackages = {"ua.com.valexa.dbismc.model"})
@EnableJpaRepositories(basePackages = {"ua.com.valexa.dbismc.repository"})
@ComponentScan(basePackages = {"ua.com.valexa.dbismc"})
public class DbIsmcApp {
    public static void main(String[] args) {
        SpringApplication.run(DbIsmcApp.class, args);
    }
}



