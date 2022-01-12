package ua.com.solidity.db.repositories;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.com.solidity.db.entities.ImportRevisionGroupRow;

import java.util.UUID;

public interface ImportRevisionGroupRowRepository extends JpaRepository<ImportRevisionGroupRow, UUID> {
    @Query(value = "Select g from import_revision_group_rows g where g.sourceGroup = :sourceGroup and g.data = :data")
    ImportRevisionGroupRow findImportRevisionGroupRowBySourceGroupAndData(@Param("sourceGroup") long sourceGroup, @Param("data") JsonNode node);
    @Query(value = "insert into import_revision_group_rows(id, source_group, revision_group, data, handled) values(:id, :sourceGroup, :revisionGroup, :data, 0) ON CONFLICT DO NOTHING", nativeQuery = true)
    ImportRevisionGroupRow softInsert(@Param("id") UUID id, @Param("sourceGroup") long sourceGroup, @Param("revisionGroup") UUID revisionGroup, @Param("data") JsonNode data);
    @Query(value = "Update import_revision_group_rows set handled = true where id = :id", nativeQuery = true)
    void markHandled(@Param("id") UUID id);
}