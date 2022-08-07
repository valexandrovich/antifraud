package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YCompanyState;

public interface YCompanyStateRepository extends JpaRepository<YCompanyState, Long> {
    Optional<YCompanyState> findByState(String state);
}
