package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YINN;

import java.util.Optional;

@Repository
public interface YINNRepository extends JpaRepository<YINN, Long> {

    Optional<YINN> findByInn(Long inn);
}
