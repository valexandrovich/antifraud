package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.db.entities.TagType;

import java.util.Optional;

public interface TagTypeRepository extends JpaRepository<TagType, Long>{
	Optional<TagType> findByCode(String code);
}
