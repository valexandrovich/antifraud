package ua.com.solidity.otp.web.controllers;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.solidity.otp.web.dto.TableCheck;
import ua.com.solidity.otp.web.repository.DBCheckRepository;

import java.util.List;

@Api(value = "DBCheckController")
@RestController
@Slf4j
public class DBCheckController {

    @Autowired
    DBCheckRepository dbCheckRepository;

    @PostMapping("/db_check")
    public List<TableCheck> checkDb()
    {
        return dbCheckRepository.getDbCheck();
    }

}
