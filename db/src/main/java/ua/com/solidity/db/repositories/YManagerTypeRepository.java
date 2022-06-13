package ua.com.solidity.db.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YManagerType;

public interface YManagerTypeRepository extends JpaRepository<YManagerType, UUID> {

    YManagerType findByType(String type);
}
