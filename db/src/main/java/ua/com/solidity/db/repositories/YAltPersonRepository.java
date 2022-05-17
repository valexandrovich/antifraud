package ua.com.solidity.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YAltPerson;

public interface YAltPersonRepository extends JpaRepository<YAltPerson, Long> {

    List<YAltPerson> findByLastName(String lastName);

    List<YAltPerson> findByFirstName(String firstName);

    List<YAltPerson> findByPatName(String patName);
}
