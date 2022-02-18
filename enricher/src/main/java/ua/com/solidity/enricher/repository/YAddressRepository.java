package ua.com.solidity.enricher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.enricher.entity.YAddress;

public interface YAddressRepository extends JpaRepository<YAddress, Long> {
}
