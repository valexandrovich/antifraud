package ua.com.solidity.db.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.ManualCTag;
import ua.com.solidity.db.entities.ManualCompany;

public interface ManualCTagRepository extends JpaRepository<ManualCTag, Long> {

    List<ManualCTag> findByCompany(ManualCompany company);
}

