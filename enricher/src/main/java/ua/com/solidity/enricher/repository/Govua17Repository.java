package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.Govua17;

public interface Govua17Repository extends PagingAndSortingRepository<Govua17, UUID> {

        Page<Govua17> findAllByPortionId(UUID portion, Pageable pageRequest);

        Long countAllByPortionId(UUID portion);
}
