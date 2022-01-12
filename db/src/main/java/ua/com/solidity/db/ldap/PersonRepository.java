package ua.com.solidity.db.ldap;

import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends LdapRepository<Person> {

    Person findByUsername(String username);
    Person findByUsernameAndPassword(String username, String password);
    List<Person> findByUsernameLikeIgnoreCase(String username);
}
