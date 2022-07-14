package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.BaseDrfo;

@Repository
public interface BaseDrfoRepository extends PagingAndSortingRepository<BaseDrfo, UUID> {

    Page<BaseDrfo> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByPortionId(UUID portion);
}
