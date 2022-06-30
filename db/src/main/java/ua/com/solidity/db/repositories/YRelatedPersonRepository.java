package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YPersonRelation;
import ua.com.solidity.db.entities.YRelatedPerson;

import java.util.Optional;

public interface YRelatedPersonRepository extends JpaRepository<YRelatedPerson, Long> {

	Optional<YRelatedPerson> findByRelation(YPersonRelation yPersonRelation);
}
