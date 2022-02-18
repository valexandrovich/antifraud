package ua.com.solidity.enricher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.enricher.entity.BaseDrfo;

import java.util.UUID;

public interface BaseDrfoRepository extends PagingAndSortingRepository<BaseDrfo, UUID> {
    Page<BaseDrfo> findAllByRevision(UUID revision, Pageable pageRequest);
}
