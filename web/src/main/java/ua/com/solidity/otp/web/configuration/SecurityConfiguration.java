package ua.com.solidity.otp.web.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ua.com.solidity.otp.web.security.filter.LoginRequestFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = "ua.com.solidity.otp.web")
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String LOGIN_ENTRY_POINT = "/authenticate";

    private static final String TOKEN_AUTH_ENTRY_POINT = "/api/**";

    @Qualifier("authSkipList")
    private final String[] authSkipList;

    private final AuthenticationSuccessHandler successHandler;

    private final AuthenticationFailureHandler failureHandler;

    private final BeanFactory context;

    private final ObjectMapper objectMapper;

    @Bean(name = "authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
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
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeRequests()
                .antMatchers(authSkipList).permitAll()
                .and()
                .authorizeRequests()
                .antMatchers(TOKEN_AUTH_ENTRY_POINT).authenticated() // Protected API End-points
                .and()
                .addFilterBefore(getLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private LoginRequestFilter getLoginProcessingFilter() {
        LoginRequestFilter loginRequestFilter = new LoginRequestFilter(LOGIN_ENTRY_POINT, successHandler, failureHandler, objectMapper);
        loginRequestFilter.setAuthenticationManager((AuthenticationManager) context.getBean("authenticationManager"));
        return loginRequestFilter;
    }
}
