package ua.com.solidity.db.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.TagType;

public interface TagTypeRepository extends JpaRepository<TagType, Long> {

    Optional<TagType> findByCode(String code);
}
