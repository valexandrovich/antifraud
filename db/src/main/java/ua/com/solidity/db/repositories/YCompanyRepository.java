package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YCompany;

public interface YCompanyRepository extends JpaRepository<YCompany, Long> {
    Optional<YCompany> findByEdrpou(Long edrpou);
}
