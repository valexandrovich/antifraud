package ua.com.solidity.db.repositories;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.com.solidity.db.entities.YPassport;

public interface YPassportRepository extends JpaRepository<YPassport, Long> {

    @EntityGraph(value = "ypassport.sources")
    Optional<YPassport> findById(Long id);

    @EntityGraph(value = "ypassport.sources")
    @Query("SELECT p FROM YPassport p " +
            "where p.number in (:numbers)")
    Set<YPassport> findPassportsByNumber(Set<Integer> numbers);
}

