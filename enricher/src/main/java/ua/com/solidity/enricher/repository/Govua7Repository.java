package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.Govua7;

public interface Govua7Repository extends PagingAndSortingRepository<Govua7, UUID> {

    Page<Govua7> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByPortionId(UUID portion);
}
