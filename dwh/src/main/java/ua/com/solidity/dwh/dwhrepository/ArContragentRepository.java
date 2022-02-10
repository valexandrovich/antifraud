package ua.com.solidity.dwh.dwhrepository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.dwh.dwhentity.ArContragent;

import java.time.LocalDate;

public interface ArContragentRepository extends JpaRepository<ArContragent, Long> {
    Iterable<ArContragent> findByArcDateAfter(LocalDate date);
}
