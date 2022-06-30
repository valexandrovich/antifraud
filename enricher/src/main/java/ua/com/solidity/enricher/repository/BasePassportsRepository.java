package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.BaseDrfo;
import ua.com.solidity.db.entities.BasePassports;

@Repository
public interface BasePassportsRepository extends PagingAndSortingRepository<BasePassports, UUID> {
    //Page<BasePassports> findAllByRevision(UUID revision, Pageable pageRequest);
    Page<BasePassports> findAllByPortionId(UUID portion, Pageable pageRequest);
    Long countAllByRevision(UUID revision);

    Long countAllByPortionId(UUID portion);
}
