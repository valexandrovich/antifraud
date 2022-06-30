package ua.com.solidity.web.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ua.com.solidity.web.security.SkipPathRequestMatcher;
import ua.com.solidity.web.security.filter.JwtRequestFilter;
import ua.com.solidity.web.security.filter.LoginRequestFilter;
import ua.com.solidity.web.security.provider.JwtAuthenticationProvider;
import ua.com.solidity.web.security.provider.LoginAuthenticationProvider;
import ua.com.solidity.web.security.service.Extractor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = "ua.com.solidity.web")
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String LOGIN_ENTRY_POINT = "/authenticate";

    private static final String TOKEN_AUTH_ENTRY_POINT = "/api/**";

    @Qualifier("authSkipList")
    private final String[] authSkipList;

    private final LoginAuthenticationProvider loginAuthenticationProvider;

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    private final AuthenticationSuccessHandler successHandler;

    private final AuthenticationFailureHandler failureHandler;

    private final Extractor extractor;

    private final BeanFactory context;

    private final ObjectMapper objectMapper;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        log.debug("Enabling Login Authentication Provider");
        auth.authenticationProvider(loginAuthenticationProvider);

        log.debug("Enabling JWT Authentication Provider");
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

    @Bean(name = "authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addExposedHeader("Location");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuring security for web applications.");

        http
                .cors()
                .and()
                .csrf()
                .disable() // We don't need CSRF for JWT based authentication
                .exceptionHandling()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(authSkipList).permitAll()
                .and()
                .authorizeRequests()
                .antMatchers(TOKEN_AUTH_ENTRY_POINT).authenticated() // Protected API End-points
                .and()
                .addFilterBefore(getLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildJwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private JwtRequestFilter buildJwtRequestFilter() {
        List<String> pathsToSkip = Arrays.asList(authSkipList);
        SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip, TOKEN_AUTH_ENTRY_POINT);

        JwtRequestFilter filter = new JwtRequestFilter(
                failureHandler,
                extractor,
                matcher
        );
        filter.setAuthenticationManager((AuthenticationManager) context.getBean("authenticationManager"));
        return filter;
    }

    private LoginRequestFilter getLoginProcessingFilter() {
        LoginRequestFilter loginRequestFilter = new LoginRequestFilter(LOGIN_ENTRY_POINT, successHandler, failureHandler, objectMapper);
        loginRequestFilter.setAuthenticationManager((AuthenticationManager) context.getBean("authenticationManager"));
        return loginRequestFilter;
    }
}
