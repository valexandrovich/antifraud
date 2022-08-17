package ua.com.solidity.web.response.secondary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ManualCompanyStatus {

    private Long companyId;

    private Integer columnIndex;

    private String message;
}
