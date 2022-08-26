package ua.com.solidity.web.security.provider;

import static ua.com.solidity.common.Chooser.chooseSecondNotNull;

import java.text.MessageFormat;
import java.util.Optional;
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
import ua.com.solidity.db.entities.Role;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.repositories.RoleMapRepository;
import ua.com.solidity.db.repositories.RoleRepository;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.web.entry.Person;
import ua.com.solidity.web.repositories.PersonRepository;
import ua.com.solidity.web.security.exception.ExtensionBadCredentialsException;
import ua.com.solidity.web.security.exception.NoSuchRoleException;
import ua.com.solidity.web.security.model.UserDetailsImpl;
import ua.com.solidity.web.service.RoleService;
import ua.com.solidity.web.service.factory.UserFactory;


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
    private final UserRepository userRepository;
    private final UserFactory userFactory;

    @Value("${user.admin.name}")
    private String adminName;
    @Value("${user.admin.password}")
    private String adminPassword;
    @Value("${user.super.name}")
    private String superName;
    @Value("${user.super.password}")
    private String superPassword;
    @Value("${user.basic.name}")
    private String basicName;
    @Value("${user.basic.password}")
    private String basicPassword;

    private static final String EMAIL_DOMAIN = "@gmail.com";
    private static final String PHONE_NUMBER_MOCK = "0500000000";

    private static final String INCORRECT_PASSWORD_MESSAGE = "Невірний пароль.";

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Attempting to verify user credentials");

        Assert.notNull(authentication, "No authentication data provided");

        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;

        String requestUserLogin = (String) authenticationToken.getPrincipal();
        String requestPassword = (String) authenticationToken.getCredentials();

        Person person;
        Role role;
        String roleName;
        UserDetailsImpl userDetails;
        if (adminName.equals(requestUserLogin)) {
            checkPassword(adminPassword, requestPassword);

            person = new Person();
            person.setDisplayName(adminName);
            person.setUsername(adminName);
            role = roleRepository.findById(3).orElseThrow(NoSuchRoleException::new);
            roleName = role.getName();

            saveUserIfNotExist(adminName,
                               role,
                               adminName,
                               adminName + EMAIL_DOMAIN,
                               PHONE_NUMBER_MOCK);

        } else if (superName.equals(requestUserLogin)) {
            checkPassword(superPassword, requestPassword);

            person = new Person();
            person.setDisplayName(superName);
            person.setUsername(superName);
            role = roleRepository.findById(1).orElseThrow(NoSuchRoleException::new);
            roleName = role.getName();

            saveUserIfNotExist(superName,
                               role,
                               superName,
                               superName + EMAIL_DOMAIN,
                               PHONE_NUMBER_MOCK);

        } else if (basicName.equals(requestUserLogin)) {
            checkPassword(basicPassword, requestPassword);

            person = new Person();
            person.setDisplayName(basicName);
            person.setUsername(basicName);
            role = roleRepository.findById(2).orElseThrow(NoSuchRoleException::new);
            roleName = role.getName();

            saveUserIfNotExist(basicName,
                               role,
                               basicName,
                               basicName + EMAIL_DOMAIN,
                               PHONE_NUMBER_MOCK);

        } else {
            person = personRepository.findByUsername(requestUserLogin)
                    .orElseThrow(() -> new ExtensionBadCredentialsException(requestUserLogin));
            role = roleService.getRoleFromMemberOf(person.getMemberOf());
            roleName = role != null ? role.getName() : null;

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
                throw new AuthenticationServiceException(INCORRECT_PASSWORD_MESSAGE);
            }

            Optional<User> userOptional = userRepository.findByUsername(requestUserLogin);
            User user;
            if (userOptional.isPresent()) {
                user = userOptional.get();
                user.setGivenName(chooseSecondNotNull(user.getGivenName(), person.getGivenName()));
                user.setSurname(chooseSecondNotNull(user.getSurname(), person.getSurname()));
                user.setFullName(chooseSecondNotNull(user.getFullName(), person.getFullName()));
                user.setDisplayName(chooseSecondNotNull(user.getDisplayName(), person.getDisplayName()));
                user.setUsername(chooseSecondNotNull(user.getUsername(), person.getUsername()));
                user.setEmail(chooseSecondNotNull(user.getEmail(), person.getEmail()));
                user.setPhoneNumber(chooseSecondNotNull(user.getPhoneNumber(), person.getPhoneNumber()));
                user.setRole(role);
            } else {
                user = userFactory.getUser(person, role);
            }
            userRepository.save(user);
        }
        userDetails = new UserDetailsImpl(person, roleName);


        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(person.getUsername(), null, null);
        token.setDetails(userDetails);

        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private void saveUserIfNotExist(
            String username,
            Role role,
            String displayName,
            String email,
            String phoneNumber
    ) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setRole(role);
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            userRepository.save(user);
        }
    }

    private void checkPassword(String savedPassword, String requestPassword) {
        if (!savedPassword.equals(requestPassword))
            throw new AuthenticationServiceException(INCORRECT_PASSWORD_MESSAGE);
    }
}
