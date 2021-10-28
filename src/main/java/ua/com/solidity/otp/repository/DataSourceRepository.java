package ua.com.solidity.otp.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.com.solidity.otp.model.DataSource;

import java.util.UUID;

@Repository
public interface DataSourceRepository extends CrudRepository<DataSource, UUID> {
}
