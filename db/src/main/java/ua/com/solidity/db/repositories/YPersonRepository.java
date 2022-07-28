package ua.com.solidity.db.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPerson;

@Repository
public interface YPersonRepository extends JpaRepository<YPerson, UUID>, JpaSpecificationExecutor<YPerson> {

    List<YPerson> findByLastNameAndFirstNameAndPatNameAndBirthdate(String lastName, String firstName,
                                                                   String patName, LocalDate birthDate);

    List<YPerson> findByFirstName(String firstName);

    List<YPerson> findByPatName(String patName);

    Page<YPerson> findBySubscribedUsers(User user, Pageable pageable);

    @EntityGraph(value = "yperson.inns")
    Optional<YPerson> findByInnsContains(YINN inn);

    @EntityGraph(value = "yperson.innsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSources")
    Optional<YPerson> findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(UUID id);

    @EntityGraph(value = "yperson.innsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSources")
    @Query("SELECT p FROM YPerson p " +
            "join p.inns i " +
            "where i.inn in (:inns)")
    Set<YPerson> findPeopleInns(Set<Long> inns);

    @EntityGraph(value = "yperson.innsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSources")
    @Query("SELECT p FROM YPerson p " +
            "join p.passports pass " +
            "where pass.id in (:idsPass)")
    Set<YPerson> findPeoplePassports(List<Long> idsPass);

    @EntityGraph(value = "yperson.forBaseEnricher")
    @Query("SELECT p FROM YPerson p " +
            "join p.passports pass " +
            "where pass.id in (:idsPass)")
    Set<YPerson> findPeoplePassportsForBaseEnricher(List<Long> idsPass);

    @EntityGraph(value = "yperson.forBaseEnricher")
    @Query("SELECT p FROM YPerson p " +
            "join p.inns i " +
            "where i.inn in (:inns)")
    Set<YPerson> findPeopleInnsForBaseEnricher(Set<Long> inns);

    @EntityGraph(value = "yperson.forBaseEnricher")
    Optional<YPerson> findForBaseEnricherById(UUID id);
}

