package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YINN;

import java.util.Optional;

@Repository
public interface YINNRepository extends JpaRepository<YINN, Long> {

    @EntityGraph(value = "yinn.sourcesAndPerson")
    Optional<YINN> findByInn(Long inn);

    @EntityGraph(value = "yinn.sourcesAndPerson")
    Optional<YINN> findById(Long id);
}
