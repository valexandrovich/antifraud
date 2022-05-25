package ua.com.solidity.common.parsers.xls;

import lombok.CustomLog;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ua.com.solidity.common.CustomParser;
import ua.com.solidity.common.ValueParser;
import ua.com.solidity.common.data.DataHeader;
import ua.com.solidity.common.data.DataObject;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@CustomLog
public class XLSParser extends CustomParser {
    public static final String PREFIX = "#";
    public static final String DEFAULT_NAMES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    XSSFWorkbook workbook;
    XSSFSheet sheet;
    int rowIndex = -1;
    int lastRowNum = -1;
    int[] columns = null;
    String[] names = null;
    DataHeader header = null;
    Object[] data = null;
    XLSDataObject rowObject = null;
    private final XLSParams params;

    public XLSParser(XLSParams params) {
        this.params = params;
    }

    private int parseColumnName(String name) {
        if (name == null) return -1;
        name = name.trim().toUpperCase(Locale.ROOT);
        int index = 0;
        if (name.startsWith(PREFIX)) {
            name = name.substring(PREFIX.length());
            index = Integer.parseInt(name);
        } else {
            for (int i = 0; i < name.length(); ++i) {
                index *= DEFAULT_NAMES.length();
                int idx = DEFAULT_NAMES.indexOf(name.charAt(i));
                if (idx < 0) return -1;
                index += idx;
            }
        }
        return index;
    }

    private int[] parseColumns() {
        String[] columnNames = params.getColumns();
        int[] res = new int[columnNames == null || columnNames.length == 0 ? 0 : columnNames.length];
        if (columnNames != null) {
            for (int i = 0; i < res.length; ++i) {
                res[i] = parseColumnName(columnNames[i]);
            }
        }
        return res;
    }

    private void setColumnName(int index, String name, String[] originalNames) {
        if (originalNames != null && index < originalNames.length && originalNames[index] != null && originalNames[index].length() > 0) {
            names[index] = originalNames[index];
        } else {
            names[index] = name != null && name.length() > 0 ? name : PREFIX + index;
        }
    }

    private void locateToNonEmptyRow() {
        boolean exists = false;
        while (rowIndex <= lastRowNum) {
            XSSFRow row = sheet.getRow(rowIndex);
            for (int i = 0; i < columns.length; ++i) {
                exists |= (data[i] = cellToObject(row.getCell(columns[i]))) != null;
            }
            if (exists) break;
            ++rowIndex;
        }
    }

    @Override
    protected boolean doOpen() {
        try {
            columns = parseColumns();
            if (columns.length == 0) return false;
            workbook = new XSSFWorkbook(stream);
            sheet = workbook.getSheetAt(params.getSheet());
            lastRowNum = sheet.getLastRowNum();
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
            Map<String, Integer> cols = new HashMap<>();
            for (int i = 0; i < names.length; ++i) {
                cols.put(names[i], i);
            }
            header = new DataHeader(cols);
            data = new Object[columns.length];
            locateToNonEmptyRow();
            return true;
        } catch (Exception e) {
            log.error("Can't open workbook.", e);
            return false;
        }
    }

    private Object cellToObject(XSSFCell cell) {
        if (cell == null) return null;
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }
        switch (type) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    if (cell.getLocalDateTimeCellValue().toInstant(ZoneOffset.UTC).equals(cell.getDateCellValue().toInstant())) {
                        return ValueParser.formatInstant(cell.getDateCellValue().toInstant());
                    } else {
                        return ValueParser.formatLocalDateTime(cell.getLocalDateTimeCellValue());
                    }
                }
                return cell.getNumericCellValue();
            case BLANK:
                return null;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            default:
                return cell.getStringCellValue();
        }
    }

    @Override
    public DataObject internalDataObject() {
        if (rowObject == null && rowIndex <= lastRowNum) {
            XSSFRow row = sheet.getRow(rowIndex);
            for (int i = 0; i < columns.length; ++i) {
                data[i] = cellToObject(row.getCell(columns[i]));
            }
            rowObject = XLSDataObject.create(header, data, rowIndex);
        }
        return rowObject;
    }

    @Override
    protected boolean doNext() {
        if (rowIndex < lastRowNum) {
            rowObject = null;
            ++rowIndex;
            locateToNonEmptyRow();
        }
        return rowIndex <= lastRowNum;
    }
}
