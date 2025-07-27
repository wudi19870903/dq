package pers.di.dataprovider.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import pers.di.common.CFileSystem;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CUtilsDateTime;
import pers.di.dataprovider.DataProvider;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import pers.di.model.*;
import pers.di.webstock.WebStock;

public class DataProviderImpl extends DataProvider
{ 
    private static final String DATA_ROOT = "./rw/data";
    private static final String STOCKLIST_FILENAME = "stocklist.xlsx";
    private static final String STOCK_DAYK_FILENAME = "dayk.txt";
    private static final String STOCK__DIVIDENTPAYOUT_FILENAME = "dividendPayout.txt";
    private Formatter s_fmt = new Formatter(System.out);

    @Override
    public String dataRoot() { 
        return DATA_ROOT;
    }

    @Override
    public int updateAllLocalStocks()
    { 
        long lTCBegin = CUtilsDateTime.GetCurrentTimeMillis();
        // 更新指数k
		String ShangZhiId = "999999";
		String ShangZhiName = "上阵指数";
		int errDownloadSZ = this.updateOneLocalStocks(ShangZhiId);
		String newestDate = "";
		if(0 == errDownloadSZ) {
			List<KLine> ctnKLine = new ArrayList<KLine>();
			int error = this.getLocalStockIDKLineList(ShangZhiId, ctnKLine);
			if(0 == error && ctnKLine.size() > 0) {
				newestDate = ctnKLine.get(ctnKLine.size()-1).date;
			}
			s_fmt.format("[%s] update success: %s (%s) item:%d date:%s\n", 
					CUtilsDateTime.GetCurDateTimeStr(), ShangZhiId, ShangZhiName, ctnKLine.size(), newestDate);
		} else {
			s_fmt.format("[%s] downloadAllStockFullData ERROR: %s error(%d)\n", 
					CUtilsDateTime.GetCurDateTimeStr(), ShangZhiId, errDownloadSZ);
			return -10;
		}

        // 更新所有k
		List<String> stockAllList = new ArrayList<String>();
		int errAllStockList = this.getLocalAllStockIDList(stockAllList);
		if (0 != errAllStockList) {
			CLog.error("DATAAPI", "DataProvider.getLocalAllStockIDList failed\n");
		}
		if(0 == errAllStockList)
		{
			int iAllStockListSize = stockAllList.size();
			for(int i = 0; i < iAllStockListSize; i++)  
	        {  
				String stockID = stockAllList.get(i);
				CObjectContainer<Integer> ctnCount = new CObjectContainer<Integer>();
				int errDownloaddStockFullData = this.updateOneLocalStocks(stockID);
	           
				double fCostTime = (CUtilsDateTime.GetCurrentTimeMillis() - lTCBegin)/1000.0f;
				
				if(0 == errDownloaddStockFullData)
				{
					List<KLine> ctnKLine = new ArrayList<KLine>();
					int errKLine = this.getLocalStockIDKLineList(stockID, ctnKLine);
		    		if(0 == errKLine && ctnKLine.size() > 0)
		    		{
		    			String stockNewestDate = ctnKLine.get(ctnKLine.size()-1).date;
		    			s_fmt.format("[%s] update success %d/%d %.3fs: %s item:%d date:%s\n", 
		    					CUtilsDateTime.GetCurDateTimeStr(), i, iAllStockListSize, fCostTime, 
		    					stockID, ctnCount.get(), stockNewestDate);
		    		}
		            else
		            {
		            	s_fmt.format("[%s] update ERROR %d/%d %.3fs: %s error(%d)\n", 
		            			CUtilsDateTime.GetCurDateTimeStr(), i, iAllStockListSize, fCostTime, 
		            			stockID, errDownloaddStockFullData);
		            }
				}
				else
				{
					s_fmt.format("[%s] update ERROR %d/%d %.3fs: %s error(%d)\n", 
							CUtilsDateTime.GetCurDateTimeStr(), i, iAllStockListSize, fCostTime, 
							stockID, errDownloaddStockFullData);
				}   
				
	        } 
			double fCostTimeAll = (CUtilsDateTime.GetCurrentTimeMillis() - lTCBegin)/1000.0f;
			s_fmt.format("[%s] update success all %.3fs, count: %d\n", 
					CUtilsDateTime.GetCurDateTimeStr(), fCostTimeAll, stockAllList.size()); 
		} else {
			s_fmt.format("[%s] downloadAllStockFullData ERROR, WebStockLayer.getAllStockList failed!\n", 
					CUtilsDateTime.GetCurDateTimeStr());
			return -1;
		}

        return 0;
    }

    @Override
    public int updateOneLocalStocks(String stockID) {
        int error = 0;
        String curDate = CUtilsDateTime.GetCurDateStr();
        String paramToDate = curDate.replace("-", "");

        List<KLine> ctnKLineNew = new ArrayList<KLine>();
        int errGetWebKLineNew = WebStock.instance().getKLine(stockID, ctnKLineNew);// 获取网络日K数据
        
        List<DividendPayout> ctnDividendPayoutNew = new ArrayList<DividendPayout>();
        int errDividendPayoutNew = WebStock.instance().getDividendPayout(stockID, ctnDividendPayoutNew);//获取网络分红派息数据
        
        if(0 == errGetWebKLineNew 
                && 0 == errDividendPayoutNew )
        {
            // 网络获取日K，分红派息 成功
            try {
                // 保存到本地
                this.saveKLine(stockID, ctnKLineNew);
                this.saveDividendPayout(stockID, ctnDividendPayoutNew);
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage()); 
                error = -21;
                return error;
            }
            
            List<KLine> ctnKLineLocalNew = new ArrayList<KLine>();
            int errKLineLocalNew = this.getLocalStockIDKLineList(stockID, ctnKLineLocalNew);
            if(errKLineLocalNew == 0) {
                //最新数据下载成功
                error = 0;
                return error;
            } else {
                error = -23;
                return error;
            }
        } else {
            // 下载日K，分红派息 失败
            error = -10;
            return error;
        }
    }

    @Override
    public int getLocalAllStockIDList(List<String> list) { 
        if (list == null) {
            return -1;
        }
        
        File file = new File(dataRoot() + "/" + STOCKLIST_FILENAME);
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

    public int getLocalStockIDKLineList(String id, List<KLine> container) {
		int error = 0;
		
		String stockDayKFileName = dataRoot() + "/" + id + "/" + STOCK_DAYK_FILENAME;
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

    private int saveKLine(String id, List<KLine> container)
	{
		String stocKLineDir = dataRoot() + "/" + id;
		if(!CFileSystem.createDir(stocKLineDir)) return -10;
		
		String stockDayKFileName = dataRoot() + "/" + id + "/" + STOCK_DAYK_FILENAME;
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
    private int saveDividendPayout(String id, List<DividendPayout> container)
	{
		String stocKLineDir = dataRoot() + "/" + id;
		if(!CFileSystem.createDir(stocKLineDir)) return -10;
		
		String stockDividendPayoutFileName = dataRoot() + "/" + id + "/" + STOCK__DIVIDENTPAYOUT_FILENAME;
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
}