package ua.com.solidity.web.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.solidity.web.dto.PersonDto;
import ua.com.solidity.web.entry.Person;
import ua.com.solidity.web.exception.PersonNotFoundException;
import ua.com.solidity.web.repositories.PersonRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LdapPersonService {

	private final PersonRepository personRepository;

	public List<String> search(final String username) {
		List<Person> personList = personRepository.findByUsernameLikeIgnoreCase(username);
		if (personList == null) {
			return Collections.emptyList();
		}

		return personList.stream()
				.map(Person::getUsername)
				.collect(Collectors.toList());
	}

	public List<Person> findAll() {
		return personRepository.findAll();
	}

	public PersonDto findByUserName(String userName) {
		Optional<Person> personOptional = personRepository.findByUsername(userName);
		Person person = personOptional.orElseThrow(() -> new PersonNotFoundException(userName));
		return new PersonDto(person);
	}
}
