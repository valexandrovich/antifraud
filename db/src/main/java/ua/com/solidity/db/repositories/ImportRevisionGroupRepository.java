package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.ImportRevisionGroup;

import java.util.UUID;

public interface ImportRevisionGroupRepository extends JpaRepository<ImportRevisionGroup, UUID> {

}