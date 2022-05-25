package ua.com.solidity.web.response;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.com.solidity.web.dto.ManualPersonDto;
import ua.com.solidity.web.response.secondary.ManualPersonStatus;
import ua.com.solidity.web.response.secondary.ManualTagStatus;

@Getter
@AllArgsConstructor
public class ValidatedManualPersonResponse {

    private List<ManualPersonDto> persons;

    private List<ManualPersonStatus> statusListPerson;

    private List<ManualTagStatus> statusListTag;

    private Set<String> wrongColumnNameList;
}
