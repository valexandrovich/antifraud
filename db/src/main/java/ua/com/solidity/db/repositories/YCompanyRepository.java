package ua.com.solidity.db.repositories;

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

    Optional<YCompany> findByEdrpou(Long edrpou);

    Page<YCompany> findByUsers(User user, Pageable pageable);

    @EntityGraph(value = "ycompany.addressesAndAltCompaniesAndTagsAndEmailsAndImportSources")
    Optional<YCompany> findWithTagsAndSourcesByEdrpou(Long edrpou);

    Optional<YCompany> findById(UUID id);

    @EntityGraph(value = "ycompany.addressesAndAltCompaniesAndTagsAndEmailsAndImportSources")
    @Query("SELECT c FROM YCompany c " +
            "where c.edrpou in (:edrpous)")
    Set<YCompany> findWithEdrpouCompanies(Set<Long> edrpous);

    @EntityGraph(value = "ycompany.addressesAndAltCompaniesAndTagsAndEmailsAndImportSources")
    @Query("SELECT c FROM YCompany c " +
            "where c.pdv in (:pdvs)")
    Set<YCompany> findWithPdvCompanies(Set<Long> pdvs);
}
