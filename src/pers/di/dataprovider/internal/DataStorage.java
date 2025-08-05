package pers.di.dataprovider.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import pers.di.common.CFileSystem;
import pers.di.common.CObjectContainer;
import pers.di.model.DividendPayout;
import pers.di.model.KLine;

public class DataStorage {
    public String dataRoot() { 
        return LocalConfig.DATA_ROOT;
    }
    public int getLocalAllStockIDList(List<String> list) { 
        if (list == null) {
            return -1;
        }
        
        File file = new File(dataRoot() + "/" + LocalConfig.STOCK_LIST_FILENAME);
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

    public int saveLocalStockIDKLineList(String id, List<KLine> container)
	{
		String stocKLineDir = dataRoot() + "/" + id;
		if(!CFileSystem.createDir(stocKLineDir)) return -10;
		
		String stockDayKFileName = dataRoot() + "/" + id + "/" + LocalConfig.STOCK_DAYK_FILENAME;
		File cfile=new File(stockDayKFileName);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile);
			for(int i = 0; i < container.size(); i++)  
	        {  
				KLine cKLine = container.get(i);  
//		            System.out.println(cKLine.date + "," 
//		            		+ cKLine.open + "," + cKLine.close);  
	            cOutputStream.write((cKLine.date + ",").getBytes());
	            cOutputStream.write((cKLine.open + ",").getBytes());
	            cOutputStream.write((cKLine.close + ",").getBytes());
	            cOutputStream.write((cKLine.low + ",").getBytes());
	            cOutputStream.write((cKLine.high + ",").getBytes());
	            cOutputStream.write((cKLine.volume + "\n").getBytes());
	        } 
			cOutputStream.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			return -1;
		}
		return 0;
	}
    public int getLocalStockIDKLineList(String id, List<KLine> container) {
		int error = 0;
		
		String stockDayKFileName = dataRoot() + "/" + id + "/" + LocalConfig.STOCK_DAYK_FILENAME;
		File cfile=new File(stockDayKFileName);

		if(!cfile.exists())
		{
			error = -10;
			return error;
		}
		
		String tempString = null;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
			int line = 1;
            while ((tempString = reader.readLine()) != null) {
            	
//                System.out.println("line " + line + ": " + tempString);
//                if(tempString.contains("2017-01-05"))
//                {
//                	System.out.println("line " + line + ": " + tempString);
//                }
                
            	KLine cKLine = new KLine();
            	String[] cols = tempString.split(",");
            	if(cols.length != 6 || cols[cols.length-1].length() <= 0) {
            		System.out.println("line " + line + ": " + tempString);
            	}
            	
            	cKLine.date = cols[0];
	        	cKLine.open = Double.parseDouble(cols[1]);
	        	cKLine.close = Double.parseDouble(cols[2]);
	        	cKLine.low = Double.parseDouble(cols[3]);
	        	cKLine.high = Double.parseDouble(cols[4]);
	        	cKLine.volume = Double.parseDouble(cols[5]);
	        	container.add(cKLine);
	        	
                line++;
            }
            reader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("ErrorInfo: BaseDataStorage.getKLine "+ "stockID:" + id + " ParseStr:" + tempString); 
			System.out.println(e.getMessage()); 
			error = -1;
			return error;
		}
		return error;
	}

	public int getDividendPayout(String id, List<DividendPayout> container)
	{
		int error = 0;
		
		String stockDividendPayoutFileName = dataRoot() + "/" + id + "/" + LocalConfig.STOCK_DIVIDENTPAYOUT_FILENAME;
		File cfile=new File(stockDividendPayoutFileName);
		if(!cfile.exists()) 
		{
			error = -10;
			return error;
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
			int line = 1;
			String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                //System.out.println("line " + line + ": " + tempString);
            	String[] cols = tempString.split(",");
            	
            	DividendPayout cDividendPayout = new DividendPayout();
            	cDividendPayout.date = cols[0];
                cDividendPayout.songGu = Double.parseDouble(cols[1]);
                cDividendPayout.zhuanGu = Double.parseDouble(cols[2]);
                cDividendPayout.paiXi = Double.parseDouble(cols[3]);
                container.add(cDividendPayout);
                
                line++;
            }
            reader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			error = -1;
			return error;
		}
		return error;
	}

    public int saveDividendPayout(String id, List<DividendPayout> container)
	{
		String stocKLineDir = dataRoot() + "/" + id;
		if(!CFileSystem.createDir(stocKLineDir)) return -10;
		
		String stockDividendPayoutFileName = dataRoot() + "/" + id + "/" + LocalConfig.STOCK_DIVIDENTPAYOUT_FILENAME;
		File cfile =new File(stockDividendPayoutFileName);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile);
			for(int i = 0; i < container.size(); i++)  
	        {  
				DividendPayout cDividendPayout = container.get(i);
				// System.out.println(cDividendPayout.date); 
				cOutputStream.write((cDividendPayout.date + ",").getBytes());
				cOutputStream.write((cDividendPayout.songGu + ",").getBytes());
				cOutputStream.write((cDividendPayout.zhuanGu + ",").getBytes());
				cOutputStream.write((cDividendPayout.paiXi + "\n").getBytes());
	        } 
			cOutputStream.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			return -1;
		}
		return 0;
	}

    public boolean saveAllStockFullDataTimestamps(String dateStr)
	{
		String allStockFullDataTimestampsFile =
            LocalConfig.DATA_ROOT + "/" + LocalConfig.STOCK_DATA_LATEST_UPDATE_DATE_FILENAME;
	
		File cfile =new File(allStockFullDataTimestampsFile);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile);
	        cOutputStream.write(dateStr.getBytes());
			cOutputStream.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			return false;
		}
		return true;
	}

    public int getAllStockFullDataTimestamps(CObjectContainer<String> container)
	{
		int error = 0;
		
		String allStockFullDataTimestampsFile =
            LocalConfig.DATA_ROOT + "/" + LocalConfig.STOCK_DATA_LATEST_UPDATE_DATE_FILENAME;

		File cfile=new File(allStockFullDataTimestampsFile);
		if(!cfile.exists()) 
		{
			error = -10;
			return error;
		}
		
		try
		{
			String encoding = "utf-8";
			InputStreamReader read = new InputStreamReader(new FileInputStream(cfile),encoding);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = bufferedReader.readLine();
            lineTxt = lineTxt.trim().replace("\n", "");
            if(lineTxt.length() == "0000-00-00".length())
            {
            	container.set(lineTxt);
            }
            read.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			error = -1;
		}
		return error;
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
}
