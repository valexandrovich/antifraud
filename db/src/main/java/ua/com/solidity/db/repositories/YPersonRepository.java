package ua.com.solidity.db.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;

@Repository
public interface YPersonRepository extends JpaRepository<YPerson, UUID>, JpaSpecificationExecutor<YPerson> {

    List<YPerson> findByLastNameAndFirstNameAndPatNameAndBirthdate(String lastName, String firstName,
                                                                   String patName, LocalDate birthDate);

    List<YPerson> findByFirstName(String firstName);

    List<YPerson> findByLastName(String lastMame);

    List<YPerson> findByPatName(String patName);

    Page<YPerson> findByUsers(User user, Pageable pageable);

    @EntityGraph(value = "yperson.inns")
    Optional<YPerson> findByInnsContains(YINN inn);

    @EntityGraph(value = "yperson.passports")
    List<YPerson> findByPassportsContains(YPassport passport);

    @EntityGraph(value = "yperson.innsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSources")
    Optional<YPerson> findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(UUID id);

    @EntityGraph(value = "yperson.passports")
    Optional<YPerson> findWithPassportsById(UUID id);

    @EntityGraph(value = "yperson.altPeople")
    Optional<YPerson> findWithAltPeopleById(UUID id);

    @EntityGraph(value = "yperson.sources")
    Optional<YPerson> findWithSourcesById(UUID id);
}
