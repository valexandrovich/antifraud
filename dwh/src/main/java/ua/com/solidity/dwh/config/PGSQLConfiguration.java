package ua.com.solidity.dwh.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@PropertySource({"classpath:dwh.properties", "classpath:application.properties"})
@EnableJpaRepositories(
        basePackages = "ua.com.solidity.db.repositories",
        entityManagerFactoryRef = "pgsqlEntityManagerFactory",
        transactionManagerRef = "pgsqlTransactionManager"
)
public class PGSQLConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "otp.pgsql.datasource")
    public DataSourceProperties pgsqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("otp.pgsql.datasource")
    public DataSource pgsqlDataSource(@Qualifier("pgsqlDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean pgsqlEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("pgsqlDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("ua.com.solidity.db.entities")
                .build();
    }

    @Bean
    public PlatformTransactionManager pgsqlTransactionManager(@Qualifier("pgsqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
