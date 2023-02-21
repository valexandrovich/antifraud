package ua.com.solidity.report.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.solidity.report.model.Column;
import ua.com.solidity.report.model.DocumentData;
import ua.com.solidity.report.service.DocumentService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static ua.com.solidity.report.utils.Utils.randomPath;

@Slf4j
@Service
public class ExcelDocumentService implements DocumentService {

    @Value("${otp.nfs.folder}")
    private String mountPoint;
    @Override
    public String createDocument(List<DocumentData> dataList) {
        log.info("Create excel document on {} records", dataList.size());
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Notification");

            setColumnsWidth(sheet);

            CellStyle headerStyle = createHeaderStyle(workbook);
            createTableHeader(sheet, headerStyle);
            log.debug("[physicalPackageMonitoringReport] Create table header");

            CellStyle bodyStyle = createBodyStyle(workbook);
            createTableBody(workbook, sheet, bodyStyle, dataList);
            log.debug("[physicalPackageMonitoringReport] Create table body");

            return writeFile(workbook);

        } catch (IOException e) {
            log.error("[physicalPackageMonitoringReport-a]", e);
            throw new RuntimeException(e);
        }
    }

    private void setColumnsWidth(Sheet sheet) {
        sheet.setColumnWidth(Column.NAME.getPosition(), Column.NAME.getColumnWidth());
        sheet.setColumnWidth(Column.UNIQUE_IDENTIFIER.getPosition(), Column.UNIQUE_IDENTIFIER.getColumnWidth());
        sheet.setColumnWidth(Column.TAG_TYPE_CODE.getPosition(), Column.TAG_TYPE_CODE.getColumnWidth());
        sheet.setColumnWidth(Column.EVENT_DATE.getPosition(), Column.EVENT_DATE.getColumnWidth());
        sheet.setColumnWidth(Column.START_DATE.getPosition(), Column.START_DATE.getColumnWidth());
        sheet.setColumnWidth(Column.END_DATE.getPosition(), Column.END_DATE.getColumnWidth());
        sheet.setColumnWidth(Column.NUMBER_VALUE.getPosition(), Column.NUMBER_VALUE.getColumnWidth());
        sheet.setColumnWidth(Column.TEXT_VALUE.getPosition(), Column.TEXT_VALUE.getColumnWidth());
        sheet.setColumnWidth(Column.DESCRIPTION.getPosition(), Column.DESCRIPTION.getColumnWidth());
        sheet.setColumnWidth(Column.SOURCE.getPosition(), Column.SOURCE.getColumnWidth());
    }

    private void createTableHeader(Sheet sheet, CellStyle style) {
        Row header = sheet.createRow(0);

        createCell(header, Column.NUMBER.getPosition(), Column.NUMBER.getName(), style);
        createCell(header, Column.NAME.getPosition(), Column.NAME.getName(), style);
        createCell(header, Column.UNIQUE_IDENTIFIER.getPosition(), Column.UNIQUE_IDENTIFIER.getName(), style);
        createCell(header, Column.TAG_TYPE_CODE.getPosition(), Column.TAG_TYPE_CODE.getName(), style);
        createCell(header, Column.EVENT_DATE.getPosition(), Column.EVENT_DATE.getName(), style);
        createCell(header, Column.START_DATE.getPosition(), Column.START_DATE.getName(), style);
        createCell(header, Column.END_DATE.getPosition(), Column.END_DATE.getName(), style);
        createCell(header, Column.NUMBER_VALUE.getPosition(), Column.NUMBER_VALUE.getName(), style);
        createCell(header, Column.TEXT_VALUE.getPosition(), Column.TEXT_VALUE.getName(), style);
        createCell(header, Column.DESCRIPTION.getPosition(), Column.DESCRIPTION.getName(), style);
        createCell(header, Column.SOURCE.getPosition(), Column.SOURCE.getName(), style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);

        style.setFont(font);

        return style;
    }

    private void createTableBody(Workbook workbook, Sheet sheet, CellStyle style, List<DocumentData> dataList) {
        int actualIndex = 1;
        for (int i = 0; i < dataList.size(); i++) {
            actualIndex = createRowsForPerson(workbook, sheet, style, actualIndex, dataList.get(i), i + 1);
        }
    }

    private int createRowsForPerson(Workbook workbook, Sheet sheet, CellStyle style, int actualIndex, DocumentData data, int dataNumber) {
        CellStyle cellStyleWithWrap = workbook.createCellStyle();
        cellStyleWithWrap.cloneStyleFrom(style);
        cellStyleWithWrap.setWrapText(true);

        for (int y = 0; y < data.getTagInformationList().size(); y++) {
            Row row = sheet.createRow(actualIndex + y);
            createCell(row, Column.NUMBER.getPosition(), String.valueOf(dataNumber), style);

            Cell cellWithName = createCell(row, Column.NAME.getPosition(), data.getName(), style);
            Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress(data.getLink());
            cellWithName.setHyperlink(hyperlink);
//            cellWithName.setCellStyle(); TODO add if needed

            createCell(row, Column.UNIQUE_IDENTIFIER.getPosition(), data.getUniqueIdentifier(), style);
            createCell(row, Column.TAG_TYPE_CODE.getPosition(), data.getTagInformationList().get(y).getTagTypeCode(), style);
            createCell(row, Column.EVENT_DATE.getPosition(), data.getTagInformationList().get(y).getEventDate(), style);
            createCell(row, Column.START_DATE.getPosition(), data.getTagInformationList().get(y).getStartDate(), style);
            createCell(row, Column.END_DATE.getPosition(), data.getTagInformationList().get(y).getEndDate(), style);
            createCell(row, Column.NUMBER_VALUE.getPosition(), data.getTagInformationList().get(y).getNumberValue(), style);
            createCell(row, Column.TEXT_VALUE.getPosition(), data.getTagInformationList().get(y).getTextValue(), cellStyleWithWrap);
            createCell(row, Column.DESCRIPTION.getPosition(), data.getTagInformationList().get(y).getDescription(), cellStyleWithWrap);
            createCell(row, Column.SOURCE.getPosition(), data.getTagInformationList().get(y).getSource(), style);
        }
        if (data.getTagInformationList().size() > 1) {
            sheet.addMergedRegion(new CellRangeAddress(
                    actualIndex, actualIndex + data.getTagInformationList().size() - 1,
                    Column.NUMBER.getPosition(), Column.NUMBER.getPosition()));
            sheet.addMergedRegion(new CellRangeAddress(
                    actualIndex, actualIndex + data.getTagInformationList().size() - 1,
                    Column.NAME.getPosition(), Column.NAME.getPosition()));
            sheet.addMergedRegion(new CellRangeAddress(
                    actualIndex, actualIndex + data.getTagInformationList().size() - 1,
                    Column.UNIQUE_IDENTIFIER.getPosition(), Column.UNIQUE_IDENTIFIER.getPosition()));
        }
        return actualIndex + data.getTagInformationList().size();
    }

    private CellStyle createBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);

        style.setFont(font);

        return style;
    }

    private Cell createCell(Row row, int position, String value, CellStyle style) {
        Cell cell = row.createCell(position);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private String writeFile(Workbook workbook) throws IOException {
        log.debug("[physicalPackageMonitoringReport] Preparing report file");
        String reportPath = randomPath() + ".xlsx";
        log.debug("[physicalPackageMonitoringReport] Report file path to be used: {}", reportPath);
        File file = new File(mountPoint, reportPath);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }
        return file.getAbsolutePath();
    }
}
