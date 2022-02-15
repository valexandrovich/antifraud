package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.com.solidity.db.entities.SchedulerEntity;

import java.util.List;

public interface SchedulerEntityRepository extends JpaRepository<SchedulerEntity, Long> {
    @Query(value = "Select s.* from scheduler s where s.enabled = true and s.force_disabled = false order by s.group_name, s.name", nativeQuery = true)
    List<SchedulerEntity> getAllEnabled();
    @Query(value = "Select 1 from scheduler_activate(:group)", nativeQuery = true)
    void activate(@Param("group") String group);
}