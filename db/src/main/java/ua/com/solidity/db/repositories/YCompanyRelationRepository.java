package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRelation;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YPerson;

public interface YCompanyRelationRepository extends JpaRepository<YCompanyRelation, Long> {
    Optional<YCompanyRelation> findByCompanyAndPersonAndRole(YCompany company, YPerson person, YCompanyRole role);
}
