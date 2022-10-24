package ua.com.solidity.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ua.com.solidity.db.entities.YPassport;

public interface YPassportRepository extends JpaRepository<YPassport, Long>, JpaSpecificationExecutor<YPassport> {

    @Query("SELECT DISTINCT p FROM YPassport p " +
            "left join fetch p.importSources p_i " +
            "where p.number = :number and p.series = :series")
    List<YPassport> findPassportsByNumberAndSeries(Integer number, String series);
}

