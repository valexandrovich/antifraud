package ua.com.solidity.db.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.Contragent;

import java.util.UUID;

@Repository
public interface ContragentRepository extends JpaRepository<Contragent, UUID> {
	Page<Contragent> findAllByRevision(UUID revision, Pageable pageRequest);
}
