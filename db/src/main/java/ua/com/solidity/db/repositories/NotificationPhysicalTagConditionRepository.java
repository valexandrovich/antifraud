package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;

public interface NotificationPhysicalTagConditionRepository extends JpaRepository<NotificationPhysicalTagCondition, Integer> {
}
