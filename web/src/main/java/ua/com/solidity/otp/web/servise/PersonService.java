package ua.com.solidity.otp.web.servise;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.ldap.Person;
import ua.com.solidity.db.ldap.PersonRepository;
import ua.com.solidity.otp.web.dto.PersonDto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public Boolean authenticate(final String username, final String password) {
        Person person = personRepository.findByUsernameAndPassword(username, password);
        return person != null;
    }

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
        Person person = personRepository.findByUsername(userName);
        PersonDto personDto = new PersonDto(person);
        return personDto;
    }


    public void modify(final String username, final String password) {
        Person person = personRepository.findByUsername(username);
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
