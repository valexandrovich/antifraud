package ua.com.solidity.db.repositories;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ua.com.solidity.db.entities.YPassport;

public interface YPassportRepository extends JpaRepository<YPassport, Long>, JpaSpecificationExecutor<YPassport> {

    @Query("SELECT p FROM YPassport p " +
            "left join fetch p.importSources p_i " +
            "where p.id = :id")
    Optional<YPassport> findById(Long id);

    @Query("SELECT p FROM YPassport p " +
            "left join fetch p.importSources p_i " +
            "where p.number in (:numbers)")
    Set<YPassport> findPassportsByNumber(Set<Integer> numbers);
}

