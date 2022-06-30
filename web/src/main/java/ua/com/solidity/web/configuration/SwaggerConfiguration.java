package ua.com.solidity.web.configuration;


import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import io.swagger.annotations.Api;
import io.swagger.models.auth.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;

import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.builders.RequestHandlerSelectors.withClassAnnotation;

import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements WebMvcConfigurer {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public Docket api(ServletContext servletContext, Environment env) {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(apis())
                .paths(PathSelectors.any())
                .build().pathMapping("/")
                .securitySchemes(Collections.singletonList(bearerToken()))
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false);
    }

    private Predicate<String> paths() {
        return Predicates.or(
                regex(".*")
        );
    }

    private Predicate<RequestHandler> apis() {
        return Predicates.and(
                basePackage("ua.com.solidity.web"),
                withClassAnnotation(Api.class)
        );
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("OTP")
                .description("")
                .version("1.0")
                .build();
    }

    @Bean
    public SecurityScheme bearerToken() {
        return new ApiKey(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER, In.HEADER.name());
    }
}
