package ua.com.solidity.db.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YManager;
import ua.com.solidity.db.entities.YManagerType;

public interface YManagerRepository extends JpaRepository<YManager, UUID> {

    Optional<YManager> findByOkpoAndInnAndType(String okpo, YINN inn, YManagerType type);
}
