package ua.com.solidity.web.dto.olap;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.web.dto.addition.RelationGroup;

@Data
public class YPersonCompareDto {
    private UUID id;
    private String lastName;
    private String firstName;
    private String patName;
    private LocalDate birthdate;
    private YINN inn;
    private YAddress address;
    private YPassport passport;
    private boolean subscribe;
    private boolean compared;
    private Set<RelationGroup> relationGroups;
}