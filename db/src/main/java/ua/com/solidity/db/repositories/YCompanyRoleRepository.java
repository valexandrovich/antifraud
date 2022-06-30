package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.YCompanyRole;

public interface YCompanyRoleRepository extends JpaRepository<YCompanyRole, Long> {
    YCompanyRole findByRole(String Role);
}
