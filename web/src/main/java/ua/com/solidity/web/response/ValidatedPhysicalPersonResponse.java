package ua.com.solidity.web.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.com.solidity.web.dto.PhysicalPersonDto;
import ua.com.solidity.web.response.secondary.CellStatus;

import java.util.List;

@Getter
@AllArgsConstructor
public class ValidatedPhysicalPersonResponse {

	private List<PhysicalPersonDto> persons;

	private List<CellStatus> cellStatuses;
}
