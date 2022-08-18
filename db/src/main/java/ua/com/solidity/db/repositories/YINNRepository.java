package ua.com.solidity.db.repositories;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YINN;

@Repository
public interface YINNRepository extends JpaRepository<YINN, Long>, JpaSpecificationExecutor<YINN> {

    @Query("SELECT DISTINCT i FROM YINN i " +
            "left join fetch i.importSources i_i " +
            "left join fetch i.person p " +
            "where i.inn = :inn")
    Optional<YINN> findByInn(Long inn);

    @Query("SELECT DISTINCT i FROM YINN i " +
            "left join fetch i.importSources i_i " +
            "left join fetch i.person p " +
            "where i.id = :id")
    Optional<YINN> findById(Long id);

    @Query("SELECT DISTINCT i FROM YINN i " +
            "left join fetch i.importSources i_i " +
            "left join fetch i.person p " +
            "where i.inn in (:inns)")
    Set<YINN> findInns(Set<Long> inns);
}
