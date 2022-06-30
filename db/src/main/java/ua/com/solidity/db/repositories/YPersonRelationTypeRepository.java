package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YPersonRelationType;

import java.util.Optional;

public interface YPersonRelationTypeRepository extends JpaRepository<YPersonRelationType, Integer> {

	Optional<YPersonRelationType> findByType(String type);
}
