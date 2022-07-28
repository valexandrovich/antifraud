package ua.com.solidity.dispatcher.model.response;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class YPersonDispatcherResponse {
    private List<UUID> resp;
    private List<UUID> temp;
}
