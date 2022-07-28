package ua.com.solidity.web.dto;

import java.util.UUID;
import lombok.Data;
import ua.com.solidity.db.entities.YCAddress;

@Data
public class YCompanySearchDto {
    private UUID id;
    private Long edrpou;
    private Long pdv;
    private String name;
    private YCAddress address;
    private boolean subscribe;
}
