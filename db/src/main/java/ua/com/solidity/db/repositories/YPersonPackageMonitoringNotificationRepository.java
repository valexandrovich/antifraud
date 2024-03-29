package ua.com.solidity.db.repositories;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YPersonPackageMonitoringNotification;

@Repository
public interface YPersonPackageMonitoringNotificationRepository extends JpaRepository<YPersonPackageMonitoringNotification, Long> {

	List<YPersonPackageMonitoringNotification> findByYpersonIdAndSent(UUID ypersonId, Boolean sent);

	List<YPersonPackageMonitoringNotification> findBySent(Boolean sent);

	Stream<YPersonPackageMonitoringNotification> findByEmailAndSent(String email, Boolean sent);
}
