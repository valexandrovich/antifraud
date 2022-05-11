package ua.com.solidity.web.repository;

import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.web.entry.Person;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends LdapRepository<Person> {

    Optional<Person> findByUsername(String userName);
    List<Person> findByUsernameLikeIgnoreCase(String userName);
}
