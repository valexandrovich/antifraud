package ua.com.solidity.db.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YPerson;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface YPersonRepository extends JpaRepository<YPerson, UUID>, JpaSpecificationExecutor<YPerson> {

	List<YPerson> findByLastNameAndFirstNameAndPatNameAndBirthdate(String lastName, String firstName,
	                                                               String patName, LocalDate birthDate);

	List<YPerson> findByFirstName(String firstName);

	List<YPerson> findByLastName(String lastMame);

	List<YPerson> findByPatName(String patName);

	Page<YPerson> findByUsers(User user, Pageable pageable);
}
