package ua.com.solidity.common.parsers.xls;

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
    private String sheetName;
    private long firstRow;
    private long lastRow;
    private int[] columns;
}
