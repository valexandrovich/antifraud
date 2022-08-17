package ua.com.solidity.web.dto;

import java.util.Set;
import java.util.UUID;
import lombok.Data;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YAltCompany;
import ua.com.solidity.db.entities.YCAddress;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompanyState;

@Data
public class YCompanyDto {
    private UUID id;
    private Long edrpou;
    private Long pdv;
    private String name;
    private YCompanyState state;
    private Set<YCAddress> addresses;
    private Set<YAltCompany> altCompanies;
    private Set<YCTag> tags;
    private Set<ImportSource> importSources;
    private boolean subscribe;
}
