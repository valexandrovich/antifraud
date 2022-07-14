package ua.com.solidity.db.repositories;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YINN;

@Repository
public interface YINNRepository extends JpaRepository<YINN, Long> {

    @EntityGraph(value = "yinn.sourcesAndPerson")
    Optional<YINN> findByInn(Long inn);

    @EntityGraph(value = "yinn.sourcesAndPerson")
    Optional<YINN> findById(Long id);

    @EntityGraph(value = "yinn.sourcesAndPerson")
    @Query("SELECT i FROM YINN i " +
            "where i.inn in (:inns)")
    Set<YINN> findInns(Set<Long> inns);
}
