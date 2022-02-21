package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YPerson;

import java.util.UUID;

@Repository
public interface YPersonRepository extends JpaRepository<YPerson, UUID>, JpaSpecificationExecutor<YPerson> {

}
