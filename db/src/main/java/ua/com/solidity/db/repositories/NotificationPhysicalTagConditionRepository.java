package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;

public interface NotificationPhysicalTagConditionRepository extends JpaRepository<NotificationPhysicalTagCondition, Integer> {
}
