package ua.com.solidity.web.dto.olap;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.entities.ImportSource;

@Getter
@Setter
public class YPassportDto {
    private Long id;
    private String series;
    private Integer number;
    private String authority;
    private LocalDate issued;
    private LocalDate endDate;
    private String recordNumber;
    private String type;
    private Boolean validity;
    private Set<ImportSource> importSources = new HashSet<>();
}
