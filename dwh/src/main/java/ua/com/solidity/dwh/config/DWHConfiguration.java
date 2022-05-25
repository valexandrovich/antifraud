package ua.com.solidity.dwh.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages ="ua.com.solidity.dwh.repositorydwh",
        entityManagerFactoryRef = "dwhEntityManagerFactory",
        transactionManagerRef = "dwhTransactionManager"
)
public class DWHConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "otp.dwh.datasource")
    public DataSourceProperties dwhDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "otp.dwh.datasource")
    public DataSource dwhDataSource(@Qualifier("dwhDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean dwhEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("dwhDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("ua.com.solidity.dwh.entities")
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager dwhTransactionManager(@Qualifier("dwhEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
