package ua.com.solidity.otp.web.servise;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.solidity.ad.entry.Person;
import ua.com.solidity.ad.repository.PersonRepository;
import ua.com.solidity.otp.web.dto.PersonDto;
import ua.com.solidity.otp.web.exception.PersonNotFoundException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {

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
        List<Person> personList = personRepository.findAll();
        return personList;
    }

    public PersonDto findByUserName(String userName) {
        Optional<Person> personOptional = personRepository.findByUsername(userName);
        Person person = personOptional.orElseThrow(() -> new PersonNotFoundException(userName));
        PersonDto personDto = new PersonDto(person);
        return personDto;
    }


    public void modify(final String userName, final String password) {
        Optional<Person> personOptional = personRepository.findByUsername(userName);
        Person person = personOptional.orElseThrow(() -> new PersonNotFoundException(userName));
        person.setPassword(password);
        personRepository.save(person);
    }

    private String digestSHA(final String password) {
        String base64;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            digest.update(password.getBytes());
            base64 = Base64.getEncoder()
                    .encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return "{SHA}" + base64;
    }
}