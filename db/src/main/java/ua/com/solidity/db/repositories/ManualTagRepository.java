package ua.com.solidity.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.db.entities.ManualTag;

public interface ManualTagRepository extends JpaRepository<ManualTag, Long> {
     List<ManualTag> findByPerson(ManualPerson person);
}
