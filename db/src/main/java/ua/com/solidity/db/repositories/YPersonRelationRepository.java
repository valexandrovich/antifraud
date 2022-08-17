package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonRelation;
import ua.com.solidity.db.entities.YPersonRelationGroup;

public interface YPersonRelationRepository extends JpaRepository<YPersonRelation, Long> {

	Optional<YPersonRelation> findByRelationGroup(YPersonRelationGroup yPersonRelationGroup);

	void deleteByPersonAndRelationGroup(YPerson person, YPersonRelationGroup relationGroup);

	Optional<YPersonRelation> findByPersonAndRelationGroup(YPerson yPerson, YPersonRelationGroup relationGroup);
}
