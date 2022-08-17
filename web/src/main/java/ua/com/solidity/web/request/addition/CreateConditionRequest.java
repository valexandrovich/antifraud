package ua.com.solidity.web.request.addition;

import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CreateConditionRequest {
    @NotNull
    @Size(min = 1, message = "Shouldn't be empty")
    private Set<Long> tagTypeIds = new HashSet<>();
}
