package ua.com.solidity.web.dto.notification;

import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;

@NoArgsConstructor
@Getter
@Setter
public class NotificationPhysicalTagMatchingDto {
    private Integer id;
    private String email;
    private String description;
    private Set<NotificationPhysicalTagCondition> conditions;
}
