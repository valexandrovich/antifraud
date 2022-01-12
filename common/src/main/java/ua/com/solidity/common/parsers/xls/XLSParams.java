package ua.com.solidity.common.parsers.xls;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
