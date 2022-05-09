package ua.com.solidity.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YPassport;

public interface YPassportRepository extends JpaRepository<YPassport, Long> {

    List<YPassport> findByNumberAndSeries(Integer number, String series);

    List<YPassport> findByNumberAndRecordNumber(Integer number,String recordNumber);
}
