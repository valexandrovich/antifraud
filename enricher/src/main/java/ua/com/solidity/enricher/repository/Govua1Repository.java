package ua.com.solidity.enricher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.Govua1;

import java.util.UUID;

public interface Govua1Repository extends PagingAndSortingRepository<Govua1, UUID> {

    Page<Govua1> findAllByPortionId(UUID portion, Pageable pageRequest);

    Long countAllByPortionId(UUID portion);
}
