package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.MonitoringNotification;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoringNotificationRepository extends JpaRepository<MonitoringNotification, Long> {

	List<MonitoringNotification> findByYpersonIdAndSent(UUID ypersonId, Boolean sent);
}
