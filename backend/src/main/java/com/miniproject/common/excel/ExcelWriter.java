package com.miniproject.common.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 공통 xlsx 생성 유틸.
 * 도메인 서비스는 헤더/행 데이터만 조립하고 이 클래스로 바이트를 만든다.
 */
public final class ExcelWriter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_COLUMN_WIDTH = 15000;

    private ExcelWriter() {
    }

    public static byte[] write(String sheetName, List<String> headers, List<List<Object>> rows) {
        Objects.requireNonNull(headers, "headers");
        Objects.requireNonNull(rows, "rows");
        String safeSheetName = (sheetName == null || sheetName.isBlank()) ? "Sheet1" : sheetName;

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(safeSheetName);
            CellStyle headerStyle = headerStyle(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i) == null ? "" : headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowIdx++);
                if (rowData == null) {
                    continue;
                }
                for (int col = 0; col < rowData.size(); col++) {
                    setCellValue(row.createCell(col), rowData.get(col));
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(width + 512, MAX_COLUMN_WIDTH));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("엑셀 파일 생성에 실패했습니다.", ex);
        }
    }

    public static byte[] write(String sheetName, String[] headers, Collection<Object[]> rows) {
        List<List<Object>> rowList = rows.stream()
                .map(arr -> arr == null ? List.<Object>of() : Arrays.<Object>asList(arr))
                .toList();
        return write(sheetName, Arrays.asList(headers), rowList);
    }

    private static CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
            return;
        }
        if (value instanceof LocalDate date) {
            cell.setCellValue(date.format(DATE));
            return;
        }
        if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(dateTime.format(DATETIME));
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }
}
