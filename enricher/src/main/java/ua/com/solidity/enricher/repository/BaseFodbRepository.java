package ua.com.solidity.enricher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.BaseFodb;

import java.util.UUID;

@Repository
public interface BaseFodbRepository extends PagingAndSortingRepository<BaseFodb, UUID> {
    Page<BaseFodb> findAllByRevision(UUID revision, Pageable pageRequest);
}