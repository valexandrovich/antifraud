package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.Govua2;

public interface Govua2Repository extends PagingAndSortingRepository<Govua2, UUID> {

    Page<Govua2> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByPortionId(UUID portion);
}
