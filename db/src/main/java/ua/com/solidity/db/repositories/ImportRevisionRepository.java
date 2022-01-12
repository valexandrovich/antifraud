package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.com.solidity.db.entities.ImportRevision;

import java.util.UUID;

public interface ImportRevisionRepository extends JpaRepository<ImportRevision, UUID> {
    @Query(value = "Select rev.* from import_revision rev where rev.source = :source order by rev.revision_date desc limit 1", nativeQuery = true)
    ImportRevision findFirstBySource(@Param("source") Long source);
}