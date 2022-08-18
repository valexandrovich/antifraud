package ua.com.solidity.db.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.Contragent;

@Repository
public interface ContragentRepository extends JpaRepository<Contragent, UUID> {

    Page<Contragent> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByRevision(UUID revision);

    Long countAllByPortionId(UUID portion);
}
