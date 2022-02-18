package ua.com.solidity.enricher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.enricher.entity.YINN;

public interface YINNRepository extends JpaRepository<YINN, Long> {
}
