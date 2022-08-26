package ua.com.solidity.web.response.secondary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ManualPersonStatus {

    private Long personId;

    private Integer columnIndex;

    private String message;
}
