package ua.com.solidity.web.request;

import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class JoinToNewRelationRequest {
    @Size(min = 2, message = "Потрібно хоча б дві людини для зв'язку")
    @NotNull(message = "Shouldn't be null")
    private Set<UUID> personIds;
    @NotNull(message = "Shouldn't be null")
    private Integer typeId;
}