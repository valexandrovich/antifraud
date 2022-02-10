package ua.com.solidity.dwh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.dwh.entity.ContragentEntity;

import java.util.UUID;

public interface ContragentRepository extends JpaRepository<ContragentEntity, UUID> {
}
