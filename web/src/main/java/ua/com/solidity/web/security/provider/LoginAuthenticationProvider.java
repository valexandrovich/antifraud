package ua.com.solidity.web.security.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ua.com.solidity.db.repositories.RoleMapRepository;
import ua.com.solidity.db.repositories.RoleRepository;
import ua.com.solidity.web.entry.Person;
import ua.com.solidity.web.repository.PersonRepository;
import ua.com.solidity.web.security.exception.ExtensionBadCredentialsException;
import ua.com.solidity.web.security.exception.NoSuchRoleException;
import ua.com.solidity.web.security.model.UserDetailsImpl;
import ua.com.solidity.web.service.RoleService;

import java.text.MessageFormat;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAuthenticationProvider implements AuthenticationProvider {

    @Value("${role.basic}")
    private String roleBasic;
    @Value("${person.base}")
    private String personBase;
    @Value("${ldap.filter}")
    private String ldapFilter;
    private final PersonRepository personRepository;
    private final LdapTemplate ldapTemplate;
    private final RoleMapRepository roleMapRepository;
    private final RoleRepository roleRepository;
    private final AuthenticatedLdapEntryContextMapper<DirContextOperations> mapper;
	private final RoleService roleService;

    @Value("${user.super.name}")
    private String superName;
    @Value("${user.super.password}")
    private String superPassword;
    @Value("${user.basic.name}")
    private String basicName;
    @Value("${user.basic.password}")
    private String basicPassword;

    private final static String PASSWORD_INCORRECT_MESSAGE = "Невірний пароль.";

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Attempting to verify user credentials");

        Assert.notNull(authentication, "No authentication data provided");

        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;

        String requestUserLogin = (String) authenticationToken.getPrincipal();
        String requestPassword = (String) authenticationToken.getCredentials();

        Person person;
        String role;
        UserDetailsImpl userDetails;
        if (superName.equals(requestUserLogin)) {
            if (!superPassword.equals(requestPassword)) throw new AuthenticationServiceException(PASSWORD_INCORRECT_MESSAGE);
            person = new Person();
            person.setDisplayName(superName);
            person.setUsername(superName);
            role = roleRepository.findById(1).orElseThrow(NoSuchRoleException::new).getName();
        } else if(basicName.equals(requestUserLogin)) {
            if (!basicPassword.equals(requestPassword)) throw new AuthenticationServiceException(PASSWORD_INCORRECT_MESSAGE);
            person = new Person();
            person.setDisplayName(basicName);
            person.setUsername(basicName);
            role = roleRepository.findById(2).orElseThrow(NoSuchRoleException::new).getName();
        } else {
            person = personRepository.findByUsername(requestUserLogin)
					.orElseThrow(() -> new ExtensionBadCredentialsException(requestUserLogin));
			role = roleService.getRoleFromMemberOf(person.getMemberOf());

            boolean authenticated;
/*
                                ---FOR FURTHER USAGE---
            DirContextOperations dco = ldapTemplate.authenticate(
                    LdapQueryBuilder.query().where("sAMAccountName").is(requestUserLogin),
                    requestPassword,
                    mapper);
            authenticated = ((dco!=null) && dco.getStringAttribute(requestUserLogin).equals(requestUserLogin));
*/

            authenticated = ldapTemplate
                    .authenticate(personBase,
                            MessageFormat.format(ldapFilter, requestUserLogin),
                            requestPassword);

            if (!authenticated) {
                throw new AuthenticationServiceException(PASSWORD_INCORRECT_MESSAGE);
            }
        }
        userDetails = new UserDetailsImpl(person, role);


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
