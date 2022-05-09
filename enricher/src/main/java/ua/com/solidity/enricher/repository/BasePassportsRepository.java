package ua.com.solidity.enricher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.BasePassports;

import java.util.UUID;

@Repository
public interface BasePassportsRepository extends PagingAndSortingRepository<BasePassports, UUID> {
    Page<BasePassports> findAllByRevision(UUID revision, Pageable pageRequest);
}
