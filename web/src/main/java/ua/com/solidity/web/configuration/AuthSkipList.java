package ua.com.solidity.web.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthSkipList {

    @Bean
    @Qualifier("authSkipList")
    public String[] getAuthWhiteList() {
        return new String[]{
                // -- swagger ui
                "/authenticate",
                "/registration",

                "/v2/api-docs",
                "/swagger-resources",
                "/documentation/swagger-ui.html",
                "/swagger-resources/**",
                "/swagger-resources/configuration/ui",
                "/swagger-resources/configuration/security",
                "/swagger-ui.html",
                "/webjars/**",
        };
    }

}
