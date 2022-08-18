package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YCompanyRole;

public interface YCompanyRoleRepository extends JpaRepository<YCompanyRole, Long> {

    Optional<YCompanyRole> findByRole(String role);
}
