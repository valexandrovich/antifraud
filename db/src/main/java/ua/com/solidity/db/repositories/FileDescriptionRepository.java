package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ua.com.solidity.db.entities.FileDescription;

import java.util.UUID;

public interface FileDescriptionRepository  extends JpaRepository<FileDescription, UUID> {


}
