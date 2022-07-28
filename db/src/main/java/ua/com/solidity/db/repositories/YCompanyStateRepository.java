package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YCompanyState;

public interface YCompanyStateRepository extends JpaRepository<YCompanyState, Long> {
    YCompanyState findByState(String State);
}
