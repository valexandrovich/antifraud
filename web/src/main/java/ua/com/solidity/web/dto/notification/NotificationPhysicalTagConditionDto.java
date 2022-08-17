package ua.com.solidity.web.dto.notification;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.db.entities.TagType;

@NoArgsConstructor
@Getter
@Setter
public class NotificationPhysicalTagConditionDto {
    private Integer id;
    private Set<TagType> tagTypes = new HashSet<>();
}
