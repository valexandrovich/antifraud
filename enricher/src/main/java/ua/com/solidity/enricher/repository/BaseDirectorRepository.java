package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.BaseDirector;

public interface BaseDirectorRepository extends PagingAndSortingRepository<BaseDirector, UUID> {

    Page<BaseDirector> findAllByRevision(UUID revision, Pageable pageRequest);
}
