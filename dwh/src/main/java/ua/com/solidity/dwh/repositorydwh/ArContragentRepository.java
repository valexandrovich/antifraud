package ua.com.solidity.dwh.repositorydwh;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.dwh.entities.ArContragent;

import java.time.LocalDate;

@Repository
public interface ArContragentRepository extends PagingAndSortingRepository<ArContragent, Long> {
    Page<ArContragent> findByArcDateAfter(LocalDate date, Pageable pageRequest);
}
