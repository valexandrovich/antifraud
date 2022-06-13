package ua.com.solidity.web.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PaginationSearchRequest {

	@Valid
	@NotNull(message = "Shouldn't be empty")
	private SearchRequest searchRequest;
	@Valid
	@NotNull(message = "Shouldn't be empty")
	private PaginationRequest paginationRequest;
}
