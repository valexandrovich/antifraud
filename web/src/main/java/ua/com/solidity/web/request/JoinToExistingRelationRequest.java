package ua.com.solidity.web.request;

import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinToExistingRelationRequest {
    @NotNull(message = "Shouldn't be null")
    private Set<UUID> personIds;
    @NotNull(message = "Shouldn't be null")
    private Long groupId;
}