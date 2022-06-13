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

import java.text.MessageFormat;
import java.util.Optional;


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

	@Value("${user.super.name}")
	private String superName;
	@Value("${user.super.password}")
	private String superPassword;
	@Value("${user.basic.name}")
	private String basicName;
	@Value("${user.basic.password}")
	private String basicPassword;
	private static final String SUPER_USER = "superuser";
	private static final String BASIC_USER = "basicuser";
	private static final String EMAIL_DOMAIN = "@gmail.com";
	private static final String PHONE_NUMBER_MOCK = "0500000000";

	private final static String INCORRECT_PASSWORD_MESSAGE = "Невірний пароль.";

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
		if (superName.equals(requestUserLogin)) {
			if (!superPassword.equals(requestPassword))
				throw new AuthenticationServiceException(INCORRECT_PASSWORD_MESSAGE);
			person = new Person();
			person.setDisplayName(superName);
			person.setUsername(superName);
			role = roleRepository.findById(1).orElseThrow(NoSuchRoleException::new);
			roleName = role.getName();
			if (userRepository.findByUsername(SUPER_USER).isEmpty()) {
				User superUser = new User();
				superUser.setUsername(SUPER_USER);
				superUser.setRole(role);
				superUser.setDisplayName(SUPER_USER);
				superUser.setEmail(SUPER_USER + EMAIL_DOMAIN);
				superUser.setPhoneNumber(PHONE_NUMBER_MOCK);
				userRepository.save(superUser);
			}
		} else if (basicName.equals(requestUserLogin)) {
			if (!basicPassword.equals(requestPassword))
				throw new AuthenticationServiceException(INCORRECT_PASSWORD_MESSAGE);
			person = new Person();
			person.setDisplayName(basicName);
			person.setUsername(basicName);
			role = roleRepository.findById(2).orElseThrow(NoSuchRoleException::new);
			roleName = role.getName();
			if (userRepository.findByUsername(BASIC_USER).isEmpty()) {
				User basicUser = new User();
				basicUser.setUsername(BASIC_USER);
				basicUser.setRole(role);
				basicUser.setDisplayName(BASIC_USER);
				basicUser.setEmail(BASIC_USER + EMAIL_DOMAIN);
				basicUser.setPhoneNumber(PHONE_NUMBER_MOCK);
				userRepository.save(basicUser);
			}
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
				if (person.getGivenName() != null) user.setGivenName(person.getGivenName());
				if (person.getSurname() != null) user.setSurname(person.getSurname());
				if (person.getFullName() != null) user.setFullName(person.getFullName());
				if (person.getDisplayName() != null) user.setDisplayName(person.getDisplayName());
				if (person.getUsername() != null) user.setUsername(person.getUsername());
				if (person.getEmail() != null) user.setEmail(person.getEmail());
				if (person.getPhoneNumber() != null) user.setPhoneNumber(person.getPhoneNumber());
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

}
