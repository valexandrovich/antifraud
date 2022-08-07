package ua.com.solidity.web.dto.olap;

import java.util.Set;
import lombok.Data;

@Data
public class RelationDto {
    private Set<YPersonCompareDto> newPeople;
    private Set<RelationGroupDto> relationGroups;
}
