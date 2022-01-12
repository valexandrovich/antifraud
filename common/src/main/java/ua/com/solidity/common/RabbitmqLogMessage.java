package ua.com.solidity.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RabbitmqLogMessage {
    private String module;
    private String code;
    private Object data;
}
