package ua.com.solidity.db.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YCompanyMonitoringNotification;

@Repository
public interface YCompanyMonitoringNotificationRepository extends JpaRepository<YCompanyMonitoringNotification, Long> {

    List<YCompanyMonitoringNotification> findByYcompanyIdAndSent(UUID ycompanyId, Boolean sent);
}
