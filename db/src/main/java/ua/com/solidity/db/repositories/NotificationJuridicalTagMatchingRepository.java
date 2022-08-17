package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.NotificationJuridicalTagMatching;

public interface NotificationJuridicalTagMatchingRepository extends JpaRepository<NotificationJuridicalTagMatching, Integer> {

    Optional<NotificationJuridicalTagMatching> findByEmail(String email);
}
