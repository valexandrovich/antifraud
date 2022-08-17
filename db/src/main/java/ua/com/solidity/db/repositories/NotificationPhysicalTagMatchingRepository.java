package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;

public interface NotificationPhysicalTagMatchingRepository extends JpaRepository<NotificationPhysicalTagMatching, Integer> {

    Optional<NotificationPhysicalTagMatching> findByEmail(String email);
}
