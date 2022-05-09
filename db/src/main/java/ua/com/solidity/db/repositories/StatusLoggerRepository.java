package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.StatusLogger;

import java.util.UUID;

@Repository
public interface StatusLoggerRepository extends JpaRepository<StatusLogger, UUID> {
}
