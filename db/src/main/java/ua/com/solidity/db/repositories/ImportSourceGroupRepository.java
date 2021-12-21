package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.com.solidity.db.entities.ImportSourceGroup;

public interface ImportSourceGroupRepository extends JpaRepository<ImportSourceGroup, Long> {
    @Query("Select g from import_source_group g where g.source = :source and g.name = :name")
    ImportSourceGroup findImportSourceGroupBySourceAndName(@Param("source") long source, @Param("name") String name);
}