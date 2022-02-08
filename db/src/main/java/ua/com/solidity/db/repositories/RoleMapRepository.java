package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.RoleMap;

import java.util.Optional;

@Repository
public interface RoleMapRepository extends JpaRepository<RoleMap, String> {

    Optional<RoleMap> findByDn(String dn);
}
