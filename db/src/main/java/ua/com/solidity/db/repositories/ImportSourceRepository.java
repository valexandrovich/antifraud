package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.com.solidity.db.entities.ImportSource;

public interface ImportSourceRepository extends JpaRepository<ImportSource, Long> {
    @Query("Select src from import_source src where src.name = :name")
    ImportSource findImportSourceByName(@Param("name") String name);
}