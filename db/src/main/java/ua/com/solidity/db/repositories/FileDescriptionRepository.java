package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.FileDescription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileDescriptionRepository  extends JpaRepository<FileDescription, UUID> {

    Optional<FileDescription> findByUuid(UUID uuid);

    List<FileDescription> findByValidated(boolean validated);
}
