package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YEmail;

@Repository
public interface YEmailRepository extends JpaRepository<YEmail, Long> {
}
