package ua.com.solidity.common.parsers.xls;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class XLSParams {
    private int sheet;
    private int rowForColumnNames = -1;
    private int firstRow = 0;
    private String[] columns;
    private String[] names;
}
