package ua.com.solidity.db.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YPersonMonitoringNotification;

@Repository
public interface YPersonMonitoringNotificationRepository extends JpaRepository<YPersonMonitoringNotification, Long> {

	List<YPersonMonitoringNotification> findByYpersonIdAndSent(UUID ypersonId, Boolean sent);
}
