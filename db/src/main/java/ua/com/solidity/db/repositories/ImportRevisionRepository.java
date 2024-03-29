package ua.com.solidity.db.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.com.solidity.db.entities.ImportRevision;

public interface ImportRevisionRepository extends JpaRepository<ImportRevision, UUID> {

    @Query(value = "Select rev.* from import_revision rev where rev.source = :source order by rev.revision_date desc limit 1", nativeQuery = true)
    ImportRevision findFirstBySource(@Param("source") Long source);

    @Query(value = "Select 1 from import_revision_remove(:id)", nativeQuery = true)
    void removeImportRevision(@Param("id") UUID id);
}