package ua.com.solidity.db.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRelation;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YPerson;

public interface YCompanyRelationRepository extends JpaRepository<YCompanyRelation, Long> {

    @Query("SELECT cr FROM YCompanyRelation cr " +
            "join cr.person p " +
            "join p.inns i " +
            "where i.inn in (:inns)")
    Set<YCompanyRelation> findRelationByInns(Set<Long> inns);

    @Query("SELECT cr FROM YCompanyRelation cr " +
            "join cr.company c " +
            "where c.edrpou in (:edrpous)")
    Set<YCompanyRelation> findRelationByEdrpous(Set<Long> edrpous);

    @Query("SELECT cr FROM YCompanyRelation cr " +
            "join cr.company c " +
            "where c.pdv in (:pdvs)")
    Set<YCompanyRelation> findRelationByPdvs(Set<Long> pdvs);
}
