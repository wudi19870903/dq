package pers.di.webstock.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import pers.di.common.CLog;
import pers.di.common.CThread;
import pers.di.webstock.IWebStock.DividendPayout;
import pers.di.webstock.IWebStock.TransactionRecord;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

public class TransactionRecordHistory extends HttpHelper 
{
	
	/*
	 * 从网络某只股票某日内的交易细节数据
	 * 
	 * 返回值：
	 *     返回0为成功，其他值为失败
	 * 参数：
	 *     上海股票：60****
	 *     深圳股票：00****或30****
	 *     container 接收容器
	 *     
	 * 其他：
	 *     doc: https://blog.csdn.net/weixin_42163573/article/details/81256348
	 *     v1.0: url: e.g. "http://market.finance.sina.com.cn/downxls.php?date=2015-02-16&symbol=sz300163"
	 *     v2.0: url: e.g. "http://market.finance.sina.com.cn/transHis.php?symbol=sz000001&date=2018-04-27&page=78"
	 *     v2.1: url: e.g. "http://stock.gtimg.cn/data/index.php?appn=detail&action=download&c=sh600103&d=20170124"
	 
	 */
	
//	public int getDayDetail(String id, String date, List<TradeDetail> container)
//	{
//		limitAccessSpeed(1500);
//		
//		int error = 0;
//		
//		// e.g "http://market.finance.sina.com.cn/downxls.php?date=2015-02-16&symbol=sz300163"
//		String urlStr = "http://market.finance.sina.com.cn/downxls.php?";
//		String tmpId = "";
//		if(id.startsWith("60") && 6 == id.length())
//		{
//			tmpId = "sh" + id;
//		}
//		else if((id.startsWith("00") ||  id.startsWith("30")) && 6 == id.length())
//		{
//			tmpId = "sz" + id;
//		}
//		else
//		{
//			error = -10;
//			return error;
//		}
//		
//		try
//		{
//			urlStr = urlStr + "date=" + date + "&symbol=" + tmpId;
//			URL url = new URL(urlStr);    
//	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();    
//	                 
//	        conn.setConnectTimeout(5*1000);  //设置连接超时间 
//	        conn.setReadTimeout(15*1000); //设置读取超时时间
//	        
//	        //防止屏蔽程序抓取而返回403错误  
//	        conn.setRequestProperty("User-Agent", getRandomUserAgent());  
//	        //得到输入流  
//	        InputStream inputStream = conn.getInputStream();   
//	        //获取自己数组  
//	        byte[] getData = readInputStream(inputStream);    
//	        String data = new String(getData, "gbk");  
//	        //System.out.println(data);
//	        String[] lines = data.split("\n");
//	        for(int i=0; i < lines.length; i++)
//	        {
//	        	if(i==0) continue;
//	        	String line = lines[i].trim();
//	        	String[] cols = line.split("\t");
//	        	
//	        	TradeDetail cTradeDetail = new TradeDetail();
//	        	cTradeDetail.time = cols[0];
//	        	cTradeDetail.price = Double.parseDouble(cols[1]);
//	        	cTradeDetail.volume = Double.parseDouble(cols[3]);
//	        	
//	        	container.add(cTradeDetail);
//	        }
//	        
//	        if(container.size() <= 0) 
//        	{
//	        	CLog.error("DATAAPI", "[DataWebStockDayDetail] cannot get data! (%s %s)", id, date);
//	        	error = -30;
//	        	return error;
//        	}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			System.out.println("Exception[WebStockDayDetail]:" + e.getMessage()); 
//			if(e.getMessage().contains("Server returned HTTP response code: 456 for URL:"))
//			{
//				CLog.error("DATAAPI", "Exception[WebStockDayDetail]:%s, need to wait!", e.getMessage());
//				// 高频访问检查, 停止6分钟
//				CThread.msleep(1000*60*6);
//			}
//			error = -1;
//        	return error;
//		}
//	
//		Collections.sort(container);
//		return error;
//	}
	

	/*
	 * 从网络某只股票某日内的交易细节数据
	 * 
	 * 返回值：
	 *     返回0为成功，其他值为失败
	 * 参数：
	 *     id: "600000"
	 *         上海股票：60****
	 *         深圳股票：00****或30****
	 *     date: "2018-07-17"
	 *     container 接收容器
	 *     
	 * 其他：
	 *     V3: url: e.g. "http://stock.gtimg.cn/data/index.php?appn=detail&action=download&c=sh600103&d=20170124"
	 *     V2: url: e.g. "http://market.finance.sina.com.cn/transHis.php?symbol=sz000001&date=2018-07-17&page=3"
	 */
	
	public int getTransactionRecordHistory(String id, String date, List<TransactionRecord> container)
	{
		int err = getTransactionRecordHistory_V3(id, date, container);
		if(0 != err)
		{
			err = getTransactionRecordHistory_V2(id, date, container);
		}
		return err;
	}
		 
	public int getTransactionRecordHistory_V2(String id, String date, List<TransactionRecord> container)
	{
		limitAccessSpeed(1500);
		
		int error = 0;
		
		// "http://market.finance.sina.com.cn/transHis.php?symbol=sz000001&date=2018-07-17&page=3"
		String urlStr = "http://market.finance.sina.com.cn/transHis.php?symbol=";

		
		if(id.contains("999999")) 
		{
			error = 0;
			return error; // 上证指数没有交易明细
		}
		
		// add symbol=sz000001
		String tmpId = "";
		if(id.startsWith("60") && 6 == id.length())
		{
			tmpId = "sh" + id;
		}
		else if((id.startsWith("00") ||  id.startsWith("30")) && 6 == id.length())
		{
			tmpId = "sz" + id;
		}
		else
		{
			error = -10;
			return error;
		}
		
		System.out.print("TransactionRecordHistory-V2-LoadPages["+tmpId+"]["+date+"]:");
		
		urlStr = urlStr + tmpId + "&date=" + date + "&page=";
		
		try{  
			int iPage = 1;
			while(true)
			{
				CThread.msleep(1600);
				String curPageUrlStr = urlStr + String.valueOf(iPage);
				
				URL url = new URL(curPageUrlStr);    
		        HttpURLConnection conn = (HttpURLConnection)url.openConnection();    

		        conn.setConnectTimeout(5*1000);  //设置连接超时间 
		        conn.setReadTimeout(15*1000); //设置读取超时时间
		        
		        //防止屏蔽程序抓取而返回403错误  
		        String randomUA = getRandomUserAgent();
		        conn.setRequestProperty("User-Agent", randomUA);  
				InputStream inputStream = conn.getInputStream(); 
				byte[] getData = readInputStream(inputStream); 
				String data = new String(getData, "gbk");  

				Parser parser = Parser.createParser(data, "utf-8");
	            TagNameFilter filter1 = new TagNameFilter("tr");  
	            NodeList trBodyList = parser.parse(filter1); 
	            
	            if(trBodyList.size() <=1)
				{
					break; // no page
				}

	            for (int i = 0; i < trBodyList.size(); i++) {
	            	
	            	Node trBodyObj = trBodyList.elementAt(i);
	            	
	            	String ColTranTime = "";
	            	String ColTranPrice = "";
	            	String ColTranCol = "";
	            	
	            	Parser parserTrBody = Parser.createParser(trBodyObj.toHtml(), "utf-8");
	                TagNameFilter filterth = new TagNameFilter("th");
	                NodeList thList = parserTrBody.parse(filterth); 
	                for (int j = 0; j < thList.size(); j++) {
	                	Node cTmpNodecol = thList.elementAt(j);
	                	String tmpStr = cTmpNodecol.toPlainTextString();
	                	
	                	if(0==j)
	                		ColTranTime = tmpStr;
	                }

	                parserTrBody = Parser.createParser(trBodyObj.toHtml(), "utf-8");
	                TagNameFilter filtertd = new TagNameFilter("td");
	                NodeList tdList = parserTrBody.parse(filtertd); 
	                for (int j = 0; j < tdList.size(); j++) {
	                	Node cTmpNodecol = tdList.elementAt(j);
	                	String tmpStr = cTmpNodecol.toPlainTextString();

	                	if(0 == j)
	                		ColTranPrice = tmpStr;
	                	else if(2 == j)
	                		ColTranCol = tmpStr;
	                }
	                //System.out.println(ColTranTime +  ColTranPrice + ColTranCol);
	                
	                if(0==i)
	                {
	                	if(!ColTranTime.equals("成交时间")  || !ColTranPrice.equals("成交价") || !ColTranCol.equals("成交量(手)") )
	                	{
	                		error = -20;
	            			return error;
	                	}
	                }
	                else
	                {
	                	TransactionRecord cTransactionRecord = new TransactionRecord();
	                	cTransactionRecord.time = ColTranTime;
	                	cTransactionRecord.price = Double.parseDouble(ColTranPrice);
	                	cTransactionRecord.volume = Double.parseDouble(ColTranCol);
	    	        	container.add(cTransactionRecord);
	                }
	            }
	            
	            
	            System.out.print(".");
	            
	            // next page
				iPage++;
			}
			System.out.print("\n");

		}
		catch(Exception e) {  
        	e.printStackTrace();
			System.out.println("Exception[DataWebStockDayDetail]:" + e.getMessage()); 
			error = -1;
        	return error;
        }  
		
		Collections.sort(container, new Comparator<TransactionRecord>() {
            @Override
			public int compare(TransactionRecord o1, TransactionRecord o2) {
                return o1.time.compareTo(o2.time);
            }
        });
		return error;
	}
	
	public int getTransactionRecordHistory_V3(String id, String date, List<TransactionRecord> container)
	{
		limitAccessSpeed(1500);
		
		int error = 0;
		
		// "http://stock.gtimg.cn/data/index.php?appn=detail&action=download&c=sh600103&d=20170124";
		String urlStr = "http://stock.gtimg.cn/data/index.php?appn=detail&action=download&c=";
		
		if(id.contains("999999")) 
		{
			error = 0;
			return error; // 上证指数没有交易明细
		}
		
		// add symbol=sz000001
		String tmpId = "";
		if(id.startsWith("60") && 6 == id.length())
		{
			tmpId = "sh" + id;
		}
		else if((id.startsWith("00") ||  id.startsWith("30")) && 6 == id.length())
		{
			tmpId = "sz" + id;
		}
		else
		{
			error = -10;
			return error;
		}
		
		System.out.print("TransactionRecordHistory-V3-Load["+tmpId+"]["+date+"]...\n");
		
		date = date.replace("-", "");
		urlStr = urlStr + tmpId + "&d=" + date;
		
		try{  
			
			URL url = new URL(urlStr);    
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();    

	        conn.setConnectTimeout(5*1000);  //设置连接超时间 
	        conn.setReadTimeout(15*1000); //设置读取超时时间
	        
	        //防止屏蔽程序抓取而返回403错误  
	        String randomUA = getRandomUserAgent();
	        conn.setRequestProperty("User-Agent", randomUA);  
			InputStream inputStream = conn.getInputStream(); 
			byte[] getData = readInputStream(inputStream); 
			String data = new String(getData, "gbk");  
			if(null == data || data.equals("暂无数据") || data.length()<10)
			{
				return -20;
			}
			
			// 
			String[] lines = data.split("\n");
			for(int i=0; i<lines.length; i++)
			{
				String line = lines[i];
				
				String[] cols = line.split("\t");
				String tranTime = cols[0];
	        	String price = cols[1];
	        	String volume = cols[3];
	        	
	        	if(cols.length < 5
	        			|| tranTime.length()!="00:00:00".length())
	        	{
	        		continue;
	        	}
	        	
	        	TransactionRecord cTransactionRecord = new TransactionRecord();
	        	cTransactionRecord.time = tranTime;
	        	cTransactionRecord.price = Double.parseDouble(price);
	        	cTransactionRecord.volume = Double.parseDouble(volume);
	        	container.add(cTransactionRecord);
			}
			
		}
		catch(Exception e) {  
        	e.printStackTrace();
			System.out.println("Exception[DataWebStockDayDetail]:" + e.getMessage()); 
			error = -1;
        	return error;
        }  
		
		Collections.sort(container, new Comparator<TransactionRecord>() {
            @Override
			public int compare(TransactionRecord o1, TransactionRecord o2) {
                return o1.time.compareTo(o2.time);
            }
        });
		
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
}
