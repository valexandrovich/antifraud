package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YPhone;

@Repository
public interface YPhoneRepository extends JpaRepository<YPhone, Long> {
}
