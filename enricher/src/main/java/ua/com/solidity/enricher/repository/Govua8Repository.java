package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.Govua8;

public interface Govua8Repository extends PagingAndSortingRepository<Govua8, UUID> {
    Page<Govua8> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByPortionId(UUID portion);
}
