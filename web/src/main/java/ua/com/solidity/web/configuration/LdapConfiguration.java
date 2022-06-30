package ua.com.solidity.web.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.NamingException;

@Configuration
@PropertySource({
        "classpath:web.properties",
        "classpath:application.properties"
})
@RequiredArgsConstructor
@EnableLdapRepositories(basePackages = "ua.com.solidity.web.repository")
public class LdapConfiguration {

    private final Environment env;

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(env.getRequiredProperty("spring.ldap.urls"));
        contextSource.setBase(env.getRequiredProperty("spring.ldap.base"));
        contextSource.setUserDn(env.getRequiredProperty("spring.ldap.username"));
        contextSource.setPassword(env.getRequiredProperty("spring.ldap.password"));
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource());
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate;
    }

    @Bean
    public AuthenticatedLdapEntryContextMapper<DirContextOperations> authenticatedLdapEntryContextMapper() {
        return (dirContext, ldapEntryIdentification) -> {
            try {
                return (DirContextOperations) dirContext.lookup(ldapEntryIdentification.getRelativeName());
            } catch (NamingException e) {
                throw new RuntimeException("lookup failed for: " + ldapEntryIdentification.getRelativeName(), e);
            }
        };
    }

}