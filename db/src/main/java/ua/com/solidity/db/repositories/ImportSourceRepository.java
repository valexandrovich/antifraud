package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.com.solidity.db.entities.ImportSource;

public interface ImportSourceRepository extends JpaRepository<ImportSource, Long> {
    @Query("Select src from import_source src where src.id = :id")
    ImportSource findImportSourceById(@Param("id") long id);
    @Query("Select src from import_source src where src.name = :name")
    ImportSource findImportSourceByName(@Param("name") String name);
    @Query(value = "Select import_source_locker(:name, :lock)", nativeQuery = true)
    boolean lockerByName(@Param("name") String name, @Param("lock") boolean lockState);
    @Query(value = "Select import_source_locker_by_id(:id, :lock)", nativeQuery = true)
    boolean lockerById(@Param("id") long id, @Param("lock") boolean lockState);
}