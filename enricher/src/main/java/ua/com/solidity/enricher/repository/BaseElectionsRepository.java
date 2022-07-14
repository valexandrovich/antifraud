package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.BaseElections;

@Repository
public interface BaseElectionsRepository extends PagingAndSortingRepository<BaseElections, UUID> {

    Page<BaseElections> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByPortionId(UUID portion);
}
