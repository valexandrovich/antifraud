package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YPassport;

public interface YPassportRepository extends JpaRepository<YPassport, Long> {

    Optional<YPassport> findByNumberAndSeries(Integer number, String series);

    Optional<YPassport> findByNumberAndRecordNumber(Integer number,String recordNumber);
}
