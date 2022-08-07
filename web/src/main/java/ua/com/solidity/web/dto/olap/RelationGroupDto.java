package ua.com.solidity.web.dto.olap;

import java.util.List;
import lombok.Data;
import ua.com.solidity.web.dto.addition.RelationGroup;

@Data
public class RelationGroupDto {
    private List<YPersonCompareDto> people;
    private RelationGroup group;
}
