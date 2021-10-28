package ua.com.solidity.otp.service;

import org.springframework.stereotype.Service;
import ua.com.solidity.otp.model.DataSource;
import ua.com.solidity.otp.repository.DataSourceRepository;

import java.util.UUID;

@Service
public class DataSourceService {

    private final DataSourceRepository dataSourceRepository;

    public DataSourceService(DataSourceRepository dataSourceRepository) {
        this.dataSourceRepository = dataSourceRepository;
    }

    public DataSource save(DataSource ds) {
        return dataSourceRepository.save(ds);
    }

    public DataSource findByUuid(UUID uuid) {
        return dataSourceRepository.findById(uuid).orElse(null);
    }
}
