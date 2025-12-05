package Functions.Utils;

import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class ExcelValues {
    /**
     * Reads an Excel file and replaces placeholders in the given template string
     * using column values from each row.
     *
     * @param excelFilePath     Path to the Excel file (.xlsx)
     * @param fileContentTemplate Template with placeholders like "Debtor_Account"
     */
    public void processExcelAndReplacePlaceholders(String excelFilePath, String fileContentTemplate) {
        FileInputStream file = null;

        try {
            file = new FileInputStream(new File(excelFilePath));
            Workbook workbook = new XSSFWorkbook(file);  // .close() is not available in POI 3.9

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            // Map headers to column indexes
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                headerMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
            }

            // Loop through all rows after the header
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String fileContent = fileContentTemplate;

                for (String columnName : headerMap.keySet()) {
                    String value = getCellValueByColumnName(row, headerMap, columnName);
                    if (value == null || value.trim().isEmpty()) {
                        value = ""; // Or "N/A"
                    }
                    fileContent = fileContent.replaceAll(columnName, value);
                }

                System.out.println("\n--- Processed content for Row " + rowIndex + " ---");
                System.out.println(fileContent);
            }

        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (file != null) file.close();  // only close the stream
            } catch (IOException e) {
                System.err.println("Error closing file stream: " + e.getMessage());
            }
        }
    }

    private String getCellValueByColumnName(Row row, Map<String, Integer> headerMap, String columnName) {
        Integer colIndex = headerMap.get(columnName);
        if (colIndex == null) return "";
        Cell cell = row.getCell(colIndex);
        return getCellValue(cell);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue().trim();
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public List<Map<String, String>> readExcelDataAsListOfMaps(String excelFilePath,String SheetName) {
        List<Map<String, String>> testDataList = new ArrayList<>();
        FileInputStream file = null;

        try {
            file = new FileInputStream(new File(excelFilePath));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheet(SheetName);

            // Read the first row from the sheet, assumed to be the header row
            Row headerRow = sheet.getRow(0);

            Map<Integer, String> headers = new HashMap<>();

            // Iterate over each cell in the header row to populate headers map
            for (Cell cell : headerRow) {
                headers.put(cell.getColumnIndex(), cell.getStringCellValue().trim());
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                // Skip if the row is null/ (blank)
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();

                // For each header, get the corresponding cell in the current row
                for (Map.Entry<Integer, String> entry : headers.entrySet()) {
                    Cell cell = row.getCell(entry.getKey());
                    // Get the cell value as a string (custom logic assumed in getCellValue method)
                    String value = getCellValue(cell);
                    // Put the value into the rowData map with the header name as the key
                    rowData.put(entry.getValue(), value);
                }
                // Add the map representing the current row to the test data list
                testDataList.add(rowData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (file != null) file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Return the list of maps containing the Excel data
        return testDataList;
    }
}
