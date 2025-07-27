package pers.di.dataprovider.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import pers.di.dataprovider.DataProvider;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class DataProviderImpl extends DataProvider
{ 
    public static final String DATA_ROOT = "./rw/data";
    public static final String STOCKLIST_FILE_PATH = "stocklist.xlsx";

    public String dataRoot() { 
        return DATA_ROOT;
    }

    @Override
    public int updateAllLocalStocks()
    { 
        return 0;
    }

    @Override
    public int getAllStockIDList(List<String> list) { 
        if (list == null) {
            return -1;
        }
        
        File file = new File(dataRoot() + "/" + STOCKLIST_FILE_PATH);
        if (!file.exists()) {
            return -2; // File not found
        }
        
        try (FileInputStream fis = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            int rowCount = 0;
            
            // Read header row to find column indices
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return -5; // No header row
            }
            
            int codeColumnIndex = -1;
            int nameColumnIndex = -1;
            
            // Find columns by header names
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerValue = getCellValueAsString(cell);
                    if ("代码".equals(headerValue) || "code".equalsIgnoreCase(headerValue)) {
                        codeColumnIndex = i;
                    } else if ("名称".equals(headerValue) || "name".equalsIgnoreCase(headerValue)) {
                        nameColumnIndex = i;
                    }
                }
            }
            
            // Check if both columns were found
            if (codeColumnIndex == -1 || nameColumnIndex == -1) {
                return -6; // Required columns not found
            }
            
            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell codeCell = row.getCell(codeColumnIndex);
                    Cell nameCell = row.getCell(nameColumnIndex);
                    
                    if (codeCell != null && nameCell != null) {
                        // Code should be numeric, name should be string
                        String code = getCellValueAsString(codeCell);
                        String name = getCellValueAsString(nameCell);
                        
                        if (code != null && !code.trim().isEmpty() && 
                            name != null && !name.trim().isEmpty()) {
                            // Combine code and name with a separator
                            //list.add(code.trim() + ":" + name.trim());
                            list.add(code.trim());
                            rowCount++;
                        }
                    }
                }
            }
            
            if (rowCount > 0) {
                return 0;
            } else {
                return -1;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            return -3; // IO error
        } catch (Exception e) {
            e.printStackTrace();
            return -4; // Other error
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        String value;
        
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = cell.getDateCellValue().toString();
                } else {
                    // For numeric values, format without decimal point if it's a whole number
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        value = String.valueOf((long) numericValue);
                    } else {
                        value = String.valueOf(numericValue);
                    }
                }
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                value = cell.getCellFormula();
                break;
            default:
                value = "";
        }
        
        // Remove leading and trailing double quotes if present
        if (value.length() >= 2 && 
            value.startsWith("\"") && 
            value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        
        return value;
    }

    public DataProviderImpl() {}
}