package ua.com.solidity.web.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationYCompanySearchRequest {
    @Valid
    @NotNull(message = "Shouldn't be empty")
    private YCompanySearchRequest searchRequest;
    @Valid
    @NotNull(message = "Shouldn't be empty")
    private PaginationRequest paginationRequest;
}
