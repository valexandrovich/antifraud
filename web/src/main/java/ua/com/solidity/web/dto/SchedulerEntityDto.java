package ua.com.solidity.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class SchedulerEntityDto {

	@NotBlank(message = "Shouldn't be blank")
	private String groupName;

	@NotBlank(message = "Shouldn't be blank")
	private String name;

	@NotBlank(message = "Shouldn't be blank")
	private String exchange;

	@NotNull(message = "Shouldn't be null")
	private JsonNode data;

	private JsonNode schedule;

	@NotNull(message = "Shouldn't be null")
	private Boolean enabled = false;

	@NotNull(message = "Shouldn't be null")
	private Boolean forceDisabled = false;
}
