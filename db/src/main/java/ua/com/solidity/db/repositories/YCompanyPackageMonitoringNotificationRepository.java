package ua.com.solidity.db.repositories;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YCompanyPackageMonitoringNotification;

@Repository
public interface YCompanyPackageMonitoringNotificationRepository extends JpaRepository<YCompanyPackageMonitoringNotification, Long> {

	List<YCompanyPackageMonitoringNotification> findByYcompanyIdAndSent(UUID ypersonId, Boolean sent);

	List<YCompanyPackageMonitoringNotification> findBySent(Boolean sent);
	Stream<YCompanyPackageMonitoringNotification> findByEmailAndSent(String email, Boolean sent);
}
