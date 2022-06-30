package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.BaseDrfo;
import ua.com.solidity.db.entities.Govua10;

public interface Govua10Repository extends PagingAndSortingRepository<Govua10, UUID> {
    //Page<Govua10> findAllByRevision(UUID revision, Pageable pageRequest);
    Page<Govua10> findAllByPortionId(UUID portion, Pageable pageRequest);
    Long countAllByRevision(UUID revision);
    Long countAllByPortionId(UUID portion);
}
