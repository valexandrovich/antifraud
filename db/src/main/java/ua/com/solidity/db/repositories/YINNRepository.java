package ua.com.solidity.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YINN;

@Repository
public interface YINNRepository extends JpaRepository<YINN, Long> {

    List<YINN> findByInn(Long inn);
}
