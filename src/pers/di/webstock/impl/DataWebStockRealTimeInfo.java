package pers.di.webstock.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pers.di.webstock.IWebStock.RealTimeInfoLite;

public class DataWebStockRealTimeInfo extends HttpHelper
{
	public DataWebStockRealTimeInfo()
	{
	}
	
	public int getRealTimeInfo(List<String> stockIDs, List<RealTimeInfoLite> container)
	{
		container.clear();
		int error = 0;
		
		if(stockIDs.size() <= 0)
		{
			return error;
		}
		
		// e.g http://hq.sinajs.cn/list=sz300163,sz300164,sh600004,
		String urlStr = "http://hq.sinajs.cn/list=";
		for(int i=0; i<stockIDs.size(); i++)
		{
			String id=stockIDs.get(i);
			String tmpId = "";
			if(id.startsWith("60") && 6 == id.length())
			{
				tmpId = "sh" + id;
			}
			else if((id.startsWith("00") ||  id.startsWith("30")) && 6 == id.length())
			{
				tmpId = "sz" + id;
			}
			else if(id.startsWith("99")) // 上证指数
			{
				tmpId = "sh" + "000001";
			}
			else
			{
				error = -10;
				return error;
			}
			
			urlStr=urlStr+tmpId+",";
		}
		
		try{  
			
			URL url = new URL(urlStr);    
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();   
	        
	        conn.setConnectTimeout(5*1000);  //设置连接超时间 
	        conn.setReadTimeout(15*1000); //设置读取超时时间 
	        
	        //防止屏蔽程序抓取而返回403错误  
	        conn.setRequestProperty("User-Agent", getRandomUserAgent());  
			InputStream inputStream = conn.getInputStream(); 
			byte[] getData = readInputStream(inputStream); 
			String data = new String(getData, "gbk");  
			//System.out.println(data);     
			
			String[] lines = data.split(";");
			for(int iLine=0;  iLine<lines.length; iLine++)
			{
				String Line=lines[iLine].trim();
				if(Line.length()<=0) continue;
				
				// every stock
				String[] cells = Line.split("\"");
				int lenCells = cells.length;
				
				String stockID = "";
				
				{
					String headdata = cells[0];
					if(headdata.contains("var hq_str_sh") || headdata.contains("var hq_str_sz"))
					{
						stockID = headdata.substring(13, 19);
					}
					else
					{
						throw new Exception("invalid string");
					}
				}
				
				{
					String detaildata = cells[1];
					//System.out.println(detaildata);     
					String[] cols = detaildata.split(",");
					
					RealTimeInfoLite cRealTimeInfo = new RealTimeInfoLite();
					
					cRealTimeInfo.stockID = stockID;
					cRealTimeInfo.name = cols[0];
					cRealTimeInfo.curPrice = Double.parseDouble(cols[3]);
					cRealTimeInfo.date = cols[30];
					cRealTimeInfo.time = cols[31];
					
					String lastCode = cols[cols.length-1];
					int iLastCode = Integer.parseInt(lastCode);

					if(cRealTimeInfo.date.length() < 2 
							|| cRealTimeInfo.name.length() < 2 
							|| iLastCode<0
							|| 0 == Double.compare(cRealTimeInfo.curPrice, 0.00f))
					{
						System.out.println("Exception[DataWebStockRealTimeInfo]: invalid data stockID:" + stockID); 
						error = -2;
						return error;
					}
					
					container.add(cRealTimeInfo);
				}
			}
			
			// check
			if(container.size() != stockIDs.size())
			{
				throw new Exception("invalid DataWebStockRealTimeInfo");
			}
			
        }catch (Exception e) {  
        	e.printStackTrace();
        	System.out.println("Exception[DataWebStockRealTimeInfo]:" + e.getMessage()); 
        	error = -1;
			return error;
        }  

		return 0;
	}
	
//	/*
//	 * 从网络获取某只股票当前信息（基本：名字 日期 时间 价格）
//	 * 
//	 * 返回值：
//	 *     返回0为成功，其他值为失败
//	 * 参数：
//	 *     container 接收容器
//	 */
//	public int getRealTimeInfo(String id, RealTimeInfo container)
//	{
//		// limitAccessSpeed(0); // not limit
//		
//		int error = 0;
//		// e.g http://hq.sinajs.cn/list=sz300163
//		String urlStr = "http://hq.sinajs.cn/list=";
//		String tmpId = "";
//		if(id.startsWith("60") && 6 == id.length())
//		{
//			tmpId = "sh" + id;
//		}
//		else if((id.startsWith("00") ||  id.startsWith("30")) && 6 == id.length())
//		{
//			tmpId = "sz" + id;
//		}
//		else if(id.startsWith("99")) // 上证指数
//		{
//			tmpId = "sh" + "000001";
//		}
//		else
//		{
//			error = -10;
//			return error;
//		}
//		urlStr = urlStr + tmpId;
//		
//		try{  
//			
//			URL url = new URL(urlStr);    
//	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();   
//	        
//	        conn.setConnectTimeout(5*1000);  //设置连接超时间 
//	        conn.setReadTimeout(15*1000); //设置读取超时时间 
//	        
//	        //防止屏蔽程序抓取而返回403错误  
//	        conn.setRequestProperty("User-Agent", getRandomUserAgent());  
//			InputStream inputStream = conn.getInputStream(); 
//			byte[] getData = readInputStream(inputStream); 
//			String data = new String(getData, "gbk");  
//			//System.out.println(data);     
//			String[] cells = data.split("\"");
//			int lenCells = cells.length;
//			String validdata = cells[lenCells - 2];
//			//System.out.println(validdata);     
//			String[] cols = validdata.split(",");
//			container.stockID = id;
//			container.name = cols[0];
//			container.curPrice = Double.parseDouble(cols[3]);
//			container.date = cols[30];
//			container.time = cols[31];
//			
//			String lastCode = cols[cols.length-1];
//			int iLastCode = Integer.parseInt(lastCode);
//			
//			if(container.date.length() < 2 || container.name.length() < 2 || iLastCode<0)
//			{
//				System.out.println("Exception[DataWebStockRealTimeInfo]: invalid data"); 
//				error = -2;
//				return error;
//			}
//			
//        }catch (Exception e) {  
//        	e.printStackTrace();
//        	System.out.println("Exception[DataWebStockRealTimeInfo]:" + e.getMessage()); 
//        	error = -1;
//			return error;
//        }  
//		return error;
//	}
	
    private byte[] readInputStream(InputStream inputStream) throws IOException {    
        byte[] buffer = new byte[1024];    
        int len = 0;    
        ByteArrayOutputStream bos = new ByteArrayOutputStream();    
        while((len = inputStream.read(buffer)) != -1) {    
            bos.write(buffer, 0, len);    
        }    
        bos.close();    
        return bos.toByteArray();    
    }  
}
