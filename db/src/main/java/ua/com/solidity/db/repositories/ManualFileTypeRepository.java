package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.ManualFileType;

public interface ManualFileTypeRepository extends JpaRepository<ManualFileType, Long> {
    Optional<ManualFileType> findByName(String name);
}
