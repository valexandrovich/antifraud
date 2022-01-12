package ua.com.solidity.common.parsers.xls;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.ValueParser;

import java.time.ZoneOffset;

@Slf4j
public class XLSParser extends CustomParser {
    XSSFWorkbook workbook;
    XSSFSheet sheet;
    int rowIndex = -1;
    int[] columns = null;
    String[] names = null;
    JsonNode lastNode = null;
    private final XLSParams params;

    public XLSParser(XLSParams params) {
        this.params = params;
    }

    private int[] parseColumns() {
        String[] columnNames = params.getColumns();
        int[] res = new int[columnNames == null || columnNames.length == 0 ? 0 : columnNames.length];
        if (columnNames != null) {
            for (int i = 0; i < res.length; ++i) {
                res[i] = parseGeneratedName(columnNames[i]);
            }
        }
        return res;
    }

    private void setColumnName(int index, String name, String[] originalNames) {
        if (originalNames != null && index < originalNames.length && originalNames[index] != null && originalNames[index].length() > 0) {
            names[index] = originalNames[index];
        } else {
            names[index] = name != null && name.length() > 0 ? name : generateNameForCounter(index);
        }
    }

    @Override
    protected boolean doOpen() {
        try {
            columns = parseColumns();
            if (columns.length == 0) return false;
            workbook = new XSSFWorkbook(stream);
            sheet = workbook.getSheetAt(params.getSheet());
            rowIndex = params.getFirstRow() - 1;
            String[] originalNames = params.getNames();
            names = new String[columns.length];
            if (params.getRowForColumnNames() >= 0) {
                XSSFRow row = sheet.getRow(params.getRowForColumnNames());
                for (int i = 0; i < columns.length; ++i) {
                    XSSFCell cell = row.getCell(columns[i]);
                    setColumnName(i, cell != null? cell.getStringCellValue() : null, originalNames);
                }
            } else {
                for (int i = 0; i < columns.length; ++i) {
                    setColumnName(i, null, originalNames);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Can't open workbook.", e);
        }
        return false;
    }

    private JsonNode cellToNode(XSSFCell cell) {
        if (cell == null) return null;
        JsonNodeFactory factory = JsonNodeFactory.instance;
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }
        switch (type) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    if (cell.getLocalDateTimeCellValue().toInstant(ZoneOffset.UTC).equals(cell.getDateCellValue().toInstant())) {
                        return factory.textNode(ValueParser.formatInstant(cell.getDateCellValue().toInstant()));
                    } else {
                        return factory.textNode(ValueParser.formatLocalDateTime(cell.getLocalDateTimeCellValue(), ZoneOffset.UTC));
                    }
                }
                return factory.numberNode(cell.getNumericCellValue());
            case BLANK:
                return null;
            case BOOLEAN:
                return factory.booleanNode(cell.getBooleanCellValue());
            default:
                return factory.textNode(cell.getStringCellValue());
        }
    }

    @Override
    public JsonNode getNode() {
        if (lastNode == null && hasData()) {
            ObjectNode res = null;
            if (rowIndex <= sheet.getLastRowNum()) {
                res = JsonNodeFactory.instance.objectNode();
                XSSFRow row = sheet.getRow(rowIndex);
                for (int i = 0; i < columns.length; ++i) {
                    res.set(names[i], cellToNode(row.getCell(columns[i])));
                }
            }
            lastNode = res;
        }
        return lastNode;
    }

    @Override
    protected boolean doNext() {
        lastNode = null;
        return ++rowIndex <= sheet.getLastRowNum();
    }
}
