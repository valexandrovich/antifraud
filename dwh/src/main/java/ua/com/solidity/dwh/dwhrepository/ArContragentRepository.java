package ua.com.solidity.dwh.dwhrepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.com.solidity.dwh.dwhentity.ArContragent;

import java.time.LocalDate;

public interface ArContragentRepository extends PagingAndSortingRepository<ArContragent, Long> {
    Page<ArContragent> findByArcDateAfter(LocalDate date, Pageable pageRequest);
}
