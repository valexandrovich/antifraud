package ua.com.solidity.web.dto.olap;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.entities.ImportSource;

@Getter
@Setter
public class YPhoneDto {
    private Long id;
    private String phone;
    private Set<ImportSource> importSources =  new HashSet<>();
}
