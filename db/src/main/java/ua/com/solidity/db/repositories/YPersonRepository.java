package ua.com.solidity.db.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YPerson;

@Repository
public interface YPersonRepository extends JpaRepository<YPerson, UUID>, JpaSpecificationExecutor<YPerson> {

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "left join fetch p.inns i " +
            "left join fetch i.importSources i_i " +
            "left join fetch p.passports pass " +
            "left join fetch pass.importSources pass_i " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.addresses adr " +
            "left join fetch adr.importSources adr_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.importSources im " +
            "left join fetch p.companyRelations c " +
            "where p.lastName = :lastName and p.firstName = :firstName and p.patName = :patName and p.birthdate = :birthDate")
    List<YPerson> findByLastNameAndFirstNameAndPatNameAndBirthdate(String lastName, String firstName,
                                                                   String patName, LocalDate birthDate);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.importSources im " +
            "where p.lastName = :lastName and p.firstName = :firstName and p.patName = :patName")
    List<YPerson> findByLastNameAndFirstNameAndPatName(String lastName, String firstName, String patName);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.importSources im " +
            "where alt.lastName = :lastName and alt.firstName = :firstName and alt.patName = :patName")
    List<YPerson> findByAltPeople(String lastName, String firstName, String patName);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "where p.id in (:ids)")
    List<YPerson> findAllInIds(List<UUID> ids);

    List<YPerson> findByFirstName(String firstName);

    List<YPerson> findByPatName(String patName);

    Page<YPerson> findBySubscribedUsers(User user, Pageable pageable);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "join fetch p.personRelations r " +
            "join fetch r.relationGroup g " +
            "where g.id = :groupId")
    List<YPerson> findAllByRelationGroupId(Long groupId);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "join fetch p.personRelations r " +
            "join fetch r.relationGroup g " +
            "where g.id in (:groupIds)")
    List<YPerson> findAllInRelationGroupIds(Set<Long> groupIds);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "join fetch p.inns i " +
            "left join fetch i.importSources i_i " +
            "left join fetch i.person i_p " +
            "where i.inn = :inn")
    Optional<YPerson> findByInnsContains(Long inn);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "left join fetch p.inns i " +
            "left join fetch i.importSources i_i " +
            "left join fetch p.passports pass " +
            "left join fetch pass.importSources pass_i " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.addresses adr " +
            "left join fetch adr.importSources adr_i " +
            "left join fetch p.phones ph " +
            "left join fetch ph.importSources ph_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.emails e " +
            "left join fetch e.importSources e_i " +
            "left join fetch p.importSources im " +
            "left join fetch p.companyRelations c " +
            "where p.id = :id")
    Optional<YPerson> findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(UUID id);

    @Query("SELECT p FROM YPerson p " +
            "join fetch p.inns i " +
            "left join fetch i.importSources i_i " +
            "left join fetch p.passports pass " +
            "left join fetch pass.importSources pass_i " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.importSources im " +
            "where i.inn in (:inns)")
    Set<YPerson> findPeopleWithInns(Set<Long> inns);

    @Query("SELECT p FROM YPerson p " +
            "left join fetch p.inns i " +
            "left join fetch i.importSources i_i " +
            "join fetch p.passports pass " +
            "left join fetch pass.importSources pass_i " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.importSources im " +
            "where pass.id in (:idsPass)")
    Set<YPerson> findPeoplePassports(List<Long> idsPass);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "left join fetch p.inns i " +
            "left join fetch i.importSources i_i " +
            "join fetch p.passports pass " +
            "left join fetch pass.importSources pass_i " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.addresses adr " +
            "left join fetch adr.importSources adr_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.importSources im " +
            "left join fetch p.companyRelations c " +
            "where pass.id in (:idsPass)")
    Set<YPerson> findPeoplePassportsForBaseEnricher(List<Long> idsPass);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "join fetch p.inns i " +
            "left join fetch i.importSources i_i " +
            "left join fetch p.passports pass " +
            "left join fetch pass.importSources pass_i " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.addresses adr " +
            "left join fetch adr.importSources adr_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.importSources im " +
            "left join fetch p.companyRelations c " +
            "where i.inn in (:inns)")
    Set<YPerson> findPeopleInnsForBaseEnricher(Set<Long> inns);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "left join fetch p.inns i " +
            "left join fetch i.importSources i_i " +
            "left join fetch p.passports pass " +
            "left join fetch pass.importSources pass_i " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "left join fetch t.importSources t_i " +
            "left join fetch p.addresses adr " +
            "left join fetch adr.importSources adr_i " +
            "left join fetch p.altPeople alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch p.importSources im " +
            "left join fetch p.companyRelations c " +
            "where p.id = :id")
    Optional<YPerson> findForBaseEnricherById(UUID id);

    @Query("SELECT DISTINCT p FROM YPerson p " +
            "left join fetch p.inns i " +
            "left join fetch p.tags t " +
            "left join fetch t.tagType t_t " +
            "where p.id = :id")
    Optional<YPerson> findWithInnsAndTagsById(UUID id);
}
