package ua.com.solidity.web.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FieldException {
    private String field;
    private String message;
}
