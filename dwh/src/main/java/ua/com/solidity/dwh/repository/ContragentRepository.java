package ua.com.solidity.dwh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.dwh.entities.Contragent;

import java.util.UUID;

@Repository
public interface ContragentRepository extends JpaRepository<Contragent, UUID> {
}
