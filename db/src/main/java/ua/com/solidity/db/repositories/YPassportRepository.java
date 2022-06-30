package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YPassport;

public interface YPassportRepository extends JpaRepository<YPassport, Long> {

    @EntityGraph(value = "ypassport.sources")
    Optional<YPassport> findByTypeAndNumberAndSeries(String type, Integer number, String series);

    @EntityGraph(value = "ypassport.sources")
    Optional<YPassport> findById(Long id);
}
