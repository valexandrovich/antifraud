package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YCompany;

import java.util.Optional;

public interface YCompanyRepository extends JpaRepository<YCompany, Long> {

    Optional<YCompany> findByEdrpou(Long edrpou);

    @EntityGraph(value = "ycompany.tagsAndSources")
    Optional<YCompany> findWithTagsAndSourcesByEdrpou(Long edrpou);
}
