package ua.com.solidity.db.repositories;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ManualCompany;

public interface ManualCompanyRepository extends JpaRepository<ManualCompany, Long> {

    List<ManualCompany> findByUuid(FileDescription uuid);

    Page<ManualCompany> findAllByUuid(FileDescription uuid, Pageable pageRequest);

    Long countAllByUuid(FileDescription uuid);
}
