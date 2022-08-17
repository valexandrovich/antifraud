package ua.com.solidity.util.model.response;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.util.model.EntityProcessing;

@Setter
@Getter
public class DispatcherResponse {
    private List<UUID> temp;
    private List<UUID> respId;
    private Set<EntityProcessing> resp;
}
