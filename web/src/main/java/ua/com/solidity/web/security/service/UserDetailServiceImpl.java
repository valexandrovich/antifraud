package ua.com.solidity.web.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.repositories.RoleRepository;
import ua.com.solidity.web.entry.Person;
import ua.com.solidity.web.repositories.PersonRepository;
import ua.com.solidity.web.security.exception.ExtensionBadCredentialsException;
import ua.com.solidity.web.security.exception.NoSuchRoleException;
import ua.com.solidity.web.security.model.UserDetailsImpl;
import ua.com.solidity.web.service.RoleService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImpl implements UserDetailsService {

	@Value("${user.super.name}")
	private String superName;
	@Value("${user.basic.name}")
	private String basicName;

	private final PersonRepository personRepository;
	private final RoleRepository roleRepository;
	private final RoleService roleService;

	@Override
	public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
		Person person;
		String role;
		UserDetailsImpl userDetails;
		if (superName.equals(name)) {
			person = new Person();
			person.setDisplayName(superName);
			role = roleRepository.findById(1).orElseThrow(NoSuchRoleException::new).getName();
		}  else if (basicName.equals(name)) {
			person = new Person();
			person.setDisplayName(basicName);
			role = roleRepository.findById(2).orElseThrow(NoSuchRoleException::new).getName();
		} else {
			person = personRepository.findByUsername(name)
					.orElseThrow(() -> new ExtensionBadCredentialsException(name));
			role = roleService.getRoleFromMemberOf(person.getMemberOf()).getName();
		}
		userDetails = new UserDetailsImpl(person, role);
		return userDetails;
	}
}
