package ua.com.solidity.otp.web.security.provider;

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
import ua.com.solidity.ad.entry.Person;
import ua.com.solidity.ad.repository.PersonRepository;
import ua.com.solidity.db.entities.RoleMap;
import ua.com.solidity.db.repositories.RoleMapRepository;
import ua.com.solidity.db.repositories.RoleRepository;
import ua.com.solidity.otp.web.security.exception.ExtensionBadCredentialsException;
import ua.com.solidity.otp.web.security.exception.NoSuchRoleException;
import ua.com.solidity.otp.web.security.model.LoginUserDetails;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAuthenticationProvider implements AuthenticationProvider {

    @Value("role.basic")
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

    @Value("${user.super.name}")
    private String superName;
    @Value("${user.super.password}")
    private String superPassword;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Attempting to verify user credentials");

        Assert.notNull(authentication, "No authentication data provided");

        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;

        String requestUserLogin = (String) authenticationToken.getPrincipal();
        String requestPassword = (String) authenticationToken.getCredentials();

        Person person;
        String role;
        LoginUserDetails userDetails;
        if (superName.equals(requestUserLogin) && superPassword.equals(requestPassword)) {
            person = new Person();
            person.setDisplayname(superName);
            role = roleRepository.findById(1).orElseThrow(NoSuchRoleException::new).getName();
            userDetails = new LoginUserDetails(person, role);
        } else {
            person = personRepository.findByUsername(requestUserLogin)
                    .orElseThrow(() -> new ExtensionBadCredentialsException(requestUserLogin));
            List<String> personGroups = person.getMemberOf()
                    .stream()
                    .map((s) -> s.substring(s.indexOf("=") + 1, s.indexOf(",")))
                    .collect(Collectors.toList());
            List<RoleMap> roleMaps = roleMapRepository.findAllById(personGroups);
            role = null;

            if (!roleMaps.isEmpty()) {
                roleMaps.sort(Comparator.comparingInt(i -> i.getRole().getId()));
                role = roleMaps.get(0).getRole().getName();
            }

            boolean authenticated;
//                                ---FOR FURTHER USAGE---
//            DirContextOperations dco = ldapTemplate.authenticate(
//                    LdapQueryBuilder.query().where("sAMAccountName").is(requestUserLogin),
//                    requestPassword,
//                    mapper);
//            authenticated = ((dco!=null) && dco.getStringAttribute(requestUserLogin).equals(requestUserLogin));

            authenticated = ldapTemplate
                    .authenticate(personBase,
                            MessageFormat.format(ldapFilter, requestUserLogin),
                            requestPassword);

            if (!authenticated) {
                throw new AuthenticationServiceException("Невірний пароль.");
            }
            userDetails = new LoginUserDetails(person, role);
        }



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
