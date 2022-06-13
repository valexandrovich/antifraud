package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.PhysicalPerson;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhysicalPersonRepository extends JpaRepository<PhysicalPerson, Long> {

	@Query("SELECT p.uuid FROM PhysicalPerson p GROUP BY p.uuid")
	List<UUID> findUuidGroupByUuid();

	List<PhysicalPerson> findByUuid(FileDescription uuid);

	List<PhysicalPerson> findByNameUkEquals(String name);
}
