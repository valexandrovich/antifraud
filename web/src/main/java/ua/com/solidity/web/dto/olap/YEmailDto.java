package ua.com.solidity.web.dto.olap;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.entities.ImportSource;

@Getter
@Setter
public class YEmailDto {
    private Long id;
    private String email;
    private Set<ImportSource> importSources =  new HashSet<>();
}
