package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.YTag;

@Repository
public interface YTagRepository extends JpaRepository<YTag, Long> {
}
