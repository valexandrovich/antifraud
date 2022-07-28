package ua.com.solidity.enricher.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class YPersonDispatcherResponse {
    private List<UUID> resp;
    private List<UUID> temp;
}
