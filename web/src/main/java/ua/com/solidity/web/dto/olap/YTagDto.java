package ua.com.solidity.web.dto.olap;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.TagType;

@Getter
@Setter
public class YTagDto {
    private Long id;
    private TagType tagType;
    private LocalDate asOf;
    private LocalDate until;
    private LocalDate eventDate;
    private String numberValue;
    private String textValue;
    private String description;
    private String source;
    private Set<ImportSource> importSources =  new HashSet<>();
}
