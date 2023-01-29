package ua.com.solidity.db.repositories;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;

public interface NotificationPhysicalTagMatchingRepository extends JpaRepository<NotificationPhysicalTagMatching, Integer> {

    Optional<NotificationPhysicalTagMatching> findByEmail(String email);
    Stream<NotificationPhysicalTagMatching> streamAllBy();
}
