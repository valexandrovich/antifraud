package ua.com.solidity.web.request;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class PaginationRequest {

	@Pattern(message = "Має бути 'ASC' або 'DESC'(не чутливий до регістру)", regexp = "^([aA][sS][cC]|[dD][eE][sS][cC])$")
	private String direction;

	@NotEmpty(message = "Потрібен хоча б 1 критерій")
	@NotNull(message = "Shouldn't be null")
	private String[] properties;

	@Min(value = 0, message = "Має бути більше або дорівнювати 0")
	@NotNull(message = "Shouldn't be null")
	private Integer page;

	@Min(value = 1, message = "Має бути більше або дорівнювати 1")
	@NotNull(message = "Shouldn't be null")
	private Integer size;

}
