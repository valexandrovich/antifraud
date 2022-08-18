package ua.com.solidity.db.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompany;

public interface YCompanyRepository extends JpaRepository<YCompany, Long>, JpaSpecificationExecutor<YCompany> {

    Page<YCompany> findByUsers(User user, Pageable pageable);

    @Query("SELECT c FROM YCompany c " +
            "left join fetch c.addresses ad " +
            "left join fetch ad.importSources ad_i " +
            "left join fetch c.altCompanies alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch c.tags t " +
            "left join fetch t.importSources t_i " +
            "left join fetch t.tagType t_t " +
            "left join fetch c.companyRelationsWithCompanies rc " +
            "left join fetch c.state st " +
            "left join fetch c.importSources im " +
            "where c.edrpou = :edrpou")
    Optional<YCompany> findWithTagsAndSourcesByEdrpou(Long edrpou);

    Optional<YCompany> findById(UUID id);

    @Query("SELECT DISTINCT c FROM YCompany c " +
            "left join fetch c.addresses ad " +
            "left join fetch ad.importSources ad_i " +
            "left join fetch c.altCompanies alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch c.tags t " +
            "left join fetch t.importSources t_i " +
            "left join fetch t.tagType t_t " +
            "left join fetch c.companyRelationsWithCompanies rc " +
            "left join fetch c.state st " +
            "left join fetch c.importSources im " +
            "where c.edrpou in (:edrpous)")
    Set<YCompany> findByEdrpous(Set<Long> edrpous);

    @Query("SELECT c FROM YCompany c " +
            "left join fetch c.addresses ad " +
            "left join fetch ad.importSources ad_i " +
            "left join fetch c.altCompanies alt " +
            "left join fetch alt.importSources alt_i " +
            "left join fetch c.tags t " +
            "left join fetch t.importSources t_i " +
            "left join fetch t.tagType t_t " +
            "left join fetch c.companyRelationsWithCompanies rc " +
            "left join fetch c.state st " +
            "left join fetch c.importSources im " +
            "where c.pdv in (:pdvs)")
    Set<YCompany> findWithPdvCompanies(Set<Long> pdvs);

    @Query("SELECT c FROM YCompany c " +
            "left join fetch c.tags t " +
            "left join fetch t.tagType t_t " +
            "where c.id in (:ids)")
    List<YCompany> findAllWithTagsInIds(List<UUID> ids);

    @Query("SELECT c FROM YCompany c " +
            "left join fetch c.tags t " +
            "left join fetch t.tagType t_t " +
            "where c.id = :id")
    Optional<YCompany> findWithTagsById(UUID id);
}
