package ua.com.valexa.dbismc.repository.sys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.dbismc.model.sys.StoredJob;

@Repository
public interface StoredJobRepository extends JpaRepository<StoredJob, Long> {
}
