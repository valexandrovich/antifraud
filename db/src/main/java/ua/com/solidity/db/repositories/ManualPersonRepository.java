package ua.com.solidity.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ManualPerson;

public interface ManualPersonRepository extends JpaRepository<ManualPerson, Long> {
    List<ManualPerson> findByUuid(FileDescription uuid);
}
