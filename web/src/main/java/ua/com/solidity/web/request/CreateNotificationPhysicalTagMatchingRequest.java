package ua.com.solidity.web.request;

import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.web.request.addition.CreateConditionRequest;

@NoArgsConstructor
@Getter
@Setter
public class CreateNotificationPhysicalTagMatchingRequest {
    private Integer id;
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}$")
    private String email;
    private String description;
    @Valid
    @Size(min = 1, message = "Shouldn't be empty")
    private Set<CreateConditionRequest> conditions;
}
