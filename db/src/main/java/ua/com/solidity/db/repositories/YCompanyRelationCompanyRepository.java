package ua.com.solidity.db.repositories;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.com.solidity.db.entities.YCompanyRelationCompany;

public interface YCompanyRelationCompanyRepository extends JpaRepository<YCompanyRelationCompany, Long> {

    @Query("SELECT cr FROM YCompanyRelationCompany cr " +
            "join cr.companyCreator c " +
            "where c.edrpou in (:edrpous)")
    Set<YCompanyRelationCompany> findRelationWithRelationCompanyByEdrpou(Set<Long> edrpous);

    @Query("SELECT cr FROM YCompanyRelationCompany cr " +
            "join cr.company c " +
            "where c.edrpou in (:edrpous)")
    Set<YCompanyRelationCompany> findRelationByEdrpou(Set<Long> edrpous);

    @Query("SELECT cr FROM YCompanyRelationCompany cr " +
            "join cr.company c " +
            "where c.pdv in (:pdvs)")
    Set<YCompanyRelationCompany> findRelationByPdv(Set<Long> pdvs);
}
