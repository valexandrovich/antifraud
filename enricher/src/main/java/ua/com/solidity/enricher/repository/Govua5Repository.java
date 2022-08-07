package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.Govua5;

public interface Govua5Repository extends PagingAndSortingRepository<Govua5, UUID> {

    Page<Govua5> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByPortionId(UUID portion);
}