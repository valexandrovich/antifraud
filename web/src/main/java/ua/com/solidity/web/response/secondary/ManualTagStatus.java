package ua.com.solidity.web.response.secondary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ManualTagStatus {

        private Long tagId;

        private Integer columnIndex;

        private String message;
}
