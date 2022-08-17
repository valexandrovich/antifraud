package ua.com.solidity.db.repositories;

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
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompany;

public interface YCompanyRepository extends JpaRepository<YCompany, Long>, JpaSpecificationExecutor<YCompany> {

    Page<YCompany> findByUsers(User user, Pageable pageable);

    @EntityGraph(value = "ycompany.addressesAndAltCompaniesAndTagsAndEmailsAndImportSources")
    Optional<YCompany> findWithTagsAndSourcesByEdrpou(Long edrpou);

    Optional<YCompany> findById(UUID id);

    @EntityGraph(value = "ycompany.addressesAndAltCompaniesAndTagsAndEmailsAndImportSources")
    @Query("SELECT c FROM YCompany c " +
            "where c.edrpou in (:edrpous)")
    Set<YCompany> finnByEdrpous(Set<Long> edrpous);

    @EntityGraph(value = "ycompany.addressesAndAltCompaniesAndTagsAndEmailsAndImportSources")
    @Query("SELECT c FROM YCompany c " +
            "where c.edrpou in (:edrpous)")
    Page<YCompany> finnByEdrpous1(Set<Long> edrpous, Pageable pageable);

    @EntityGraph(value = "ycompany.addressesAndAltCompaniesAndTagsAndEmailsAndImportSources")
    @Query("SELECT c FROM YCompany c " +
            "where c.pdv in (:pdvs)")
    Set<YCompany> findWithPdvCompanies(Set<Long> pdvs);

    @EntityGraph(value = "ycompany.tagsTagType")
    @Query("SELECT c FROM YCompany c " +
            "where c.id in (:ids)")
    List<YCompany> findAllWithTagsInIds(List<UUID> ids);

    @EntityGraph(value = "ycompany.tagsTagType")
    Optional<YCompany> findWithTagsById(UUID id);
}
