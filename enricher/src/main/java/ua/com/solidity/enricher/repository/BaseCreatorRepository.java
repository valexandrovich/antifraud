package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.BaseCreator;

public interface BaseCreatorRepository extends PagingAndSortingRepository<BaseCreator, UUID> {

    Page<BaseCreator> findAllByRevision(UUID revision, Pageable pageRequest);
}
