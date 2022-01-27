package ua.com.solidity.otp.web.security.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ua.com.solidity.ad.entry.Person;
import ua.com.solidity.ad.repository.PersonRepository;
import ua.com.solidity.otp.web.security.exception.ExtensionBadCredentialsException;
import ua.com.solidity.otp.web.security.model.LoginUserDetails;
import ua.com.solidity.otp.web.security.model.Role;

import java.text.MessageFormat;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAuthenticationProvider implements AuthenticationProvider {

    @Value("${person.base}")
    private String personBase;
    @Value("${ldap.filter}")
    private String ldapFilter;
    private final PersonRepository personRepository;
    private final LdapTemplate ldapTemplate;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Attempting to verify user credentials");

        Assert.notNull(authentication, "No authentication data provided");

        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;

        String requestUserLogin = (String) authenticationToken.getPrincipal();
        String requestPassword = (String) authenticationToken.getCredentials();

        Person person = personRepository.findByUsername(requestUserLogin)
                .orElseThrow(() -> new ExtensionBadCredentialsException(requestUserLogin));

        boolean authenticated = ldapTemplate
                .authenticate(personBase,
                        MessageFormat.format(ldapFilter, requestUserLogin),
                        requestPassword);

        if(!authenticated) {
            throw new AuthenticationServiceException("Невірний пароль.");
        }
        Role role;
        switch (requestUserLogin) {
            case "BieloienkoV": role = Role.ADMIN; break;
            case "vb": role = Role.ADMIN; break;
            case "dr": role = Role.ADVANCED; break;
            case "kc": role = Role.ADVANCED; break;
            case "av": role = Role.ADVANCED; break;
            default: role = Role.BASIC;
        }

        LoginUserDetails userDetails = new LoginUserDetails(person, role);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(person.getUsername(), null, null);
        token.setDetails(userDetails);

        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
