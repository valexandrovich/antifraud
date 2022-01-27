package ua.com.solidity.ad.repository;

import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.ad.entry.Person;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends LdapRepository<Person> {

    Optional<Person> findByUsername(String username);
    List<Person> findByUsernameLikeIgnoreCase(String username);
}
