package ua.com.solidity.web.service.factory;

import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.Role;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.web.entry.Person;

@Component
public class UserFactory {

	public User getUser(Person person, Role role) {
		User user = new User();
		user.setGivenName(person.getGivenName());
		user.setSurname(person.getSurname());
		user.setFullName(person.getFullName());
		user.setDisplayName(person.getDisplayName());
		user.setUsername(person.getUsername());
		user.setEmail(person.getEmail());
		user.setPhoneNumber(person.getPhoneNumber());
		user.setRole(role);

		return user;
	}
}
