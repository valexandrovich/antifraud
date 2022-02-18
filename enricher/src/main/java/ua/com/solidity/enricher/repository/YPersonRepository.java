package ua.com.solidity.enricher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.enricher.entity.YPerson;

import java.util.UUID;

public interface YPersonRepository extends JpaRepository<YPerson, UUID> {
}
