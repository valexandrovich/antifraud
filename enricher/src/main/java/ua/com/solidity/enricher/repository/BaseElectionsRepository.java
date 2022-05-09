package ua.com.solidity.enricher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.BaseElections;

import java.util.UUID;

@Repository
public interface BaseElectionsRepository extends PagingAndSortingRepository<BaseElections, UUID> {
    Page<BaseElections> findAllByRevision(UUID revision, Pageable pageRequest);
}
