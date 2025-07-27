package pers.di.webstock.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import pers.di.webstock.IWebStock.RealTimeInfoLite;
import pers.di.webstock.IWebStock.StockInfo;

public class DataWebStockInfo extends HttpHelper
{
	public DataWebStockInfo()
	{
		m_cDataWebStockRealTimeInfo = new DataWebStockRealTimeInfo();
	}
	
	/*
	 * 从网络获取某只股票信息（基本信息，总市值，流通市值，市盈率等）
	 * 
	 * 返回值：
	 *     返回0为成功，其他值为失败
	 * 参数：
	 *     container 接收容器
	 */
	public int getStockInfo(String id, StockInfo container)
	{
		// limitAccessSpeed(0); // not limit
		
		int error = 0;
		
		// get base info
		List<RealTimeInfoLite> ctnRealTimeInfos = new ArrayList<RealTimeInfoLite>();
		List ids = new ArrayList<String>();
		ids.add(id);
		int errGetRealTimeInfo = m_cDataWebStockRealTimeInfo.getRealTimeInfo(ids, ctnRealTimeInfos);
		if(0 != errGetRealTimeInfo) 
		{
			error = -2;
			return error;
		}
		
		RealTimeInfoLite ctnRealTimeInfo = ctnRealTimeInfos.get(0);
		
		container.name = ctnRealTimeInfo.name;
		container.date = ctnRealTimeInfo.date;
		container.time = ctnRealTimeInfo.time;
		container.curPrice = ctnRealTimeInfo.curPrice;
		
		// e.g http://qt.gtimg.cn/q=sz000858
		String urlStr = "http://qt.gtimg.cn/q=";
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
			tmpId = "sh" + "000001"; // 上证指数没有更多基本信息
			error = 0;
			return error;
		}
		else
		{
			error = -10;
			return error;
		}
		urlStr = urlStr + tmpId;
		
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
			String[] cells = data.split("~");
//			for(int i =0; i< cells.length; i++)
//			{
//				System.out.println(cells[i]);
//			}
			container.allMarketValue = Double.parseDouble(cells[45]); //总市值
			container.circulatedMarketValue = Double.parseDouble(cells[44]); // 流通市值
			if(cells[39].length() != 0)
				container.peRatio = Double.parseDouble(cells[39]); //市盈率
			else
				container.peRatio = 0.0f;
			
        }catch (Exception e) {  
        	e.printStackTrace();
        	System.out.println("Exception[getRealTimeInfoMore]:" + e.getMessage()); 
        	error = -1;
        	return error;
        }  
		return error;
	}
	
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
    
    private DataWebStockRealTimeInfo m_cDataWebStockRealTimeInfo;
}
