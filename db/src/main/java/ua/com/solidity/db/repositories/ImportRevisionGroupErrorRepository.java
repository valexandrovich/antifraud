package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.ImportRevisionGroupError;

import java.util.UUID;

public interface ImportRevisionGroupErrorRepository extends JpaRepository<ImportRevisionGroupError, UUID> {

}