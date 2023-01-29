package ua.com.solidity.enricher.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.db.entities.Govua9;

public interface Govua9Repository extends PagingAndSortingRepository<Govua9, UUID> {

        Page<Govua9> findAllByPortionId(UUID portion, Pageable pageRequest);

        Long countAllByPortionId(UUID portion);
}
