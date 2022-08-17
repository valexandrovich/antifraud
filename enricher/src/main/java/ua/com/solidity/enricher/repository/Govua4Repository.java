package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.Govua4;

public interface Govua4Repository extends PagingAndSortingRepository<Govua4, UUID> {

    Page<Govua4> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByPortionId(UUID portion);
}