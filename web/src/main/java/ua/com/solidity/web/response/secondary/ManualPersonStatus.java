package ua.com.solidity.web.response.secondary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ManualPersonStatus {

    private Long personId;

    private Integer columnIndex;

    private String message;
}
