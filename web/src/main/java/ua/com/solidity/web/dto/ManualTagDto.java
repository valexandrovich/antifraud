package ua.com.solidity.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ManualTagDto {
    private Long id;
    private String mkId;
    private String mkEventDate;
    private String mkStart;
    private String mkExpire;
    private String mkNumberValue;
    private String mkTextValue;
    private String mkDescription;
    private String mkSource;
}
