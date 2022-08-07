package ua.com.solidity.web.response;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.com.solidity.web.dto.dynamicfile.ManualCompanyDto;
import ua.com.solidity.web.response.secondary.ManualCompanyStatus;
import ua.com.solidity.web.response.secondary.ManualTagStatus;

@Getter
@AllArgsConstructor
public class ValidatedManualCompanyResponse {

    private List<ManualCompanyDto> companies;

    private List<ManualCompanyStatus> statusListCompany;

    private List<ManualTagStatus> statusListTag;

    private Set<String> wrongColumnNameList;
}
