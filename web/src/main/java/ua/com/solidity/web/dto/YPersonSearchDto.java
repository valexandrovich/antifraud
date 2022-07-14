package ua.com.solidity.web.dto;

import lombok.Data;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class YPersonSearchDto {
    private UUID id;
    private String lastName;
    private String firstName;
    private String patName;
    private LocalDate birthdate;
    private YINN inn;
    private YAddress address;
    private YPassport passport;
    private boolean subscribe;
}