package pers.di.webstock.impl;

import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import pers.di.common.CHttp;
import pers.di.model.*;

public class DataWebStockDayK {
	
	public DataWebStockDayK()
	{
		m_http = new CHttp();
	}
	
	/*
	 * 从网络获取某只股票的日K数据
	 * 
	 * 返回值：
	 *     返回0为成功，其他值为失败
	 * 参数：
	 *     传入999999代表上证指数
	 *     上海股票：60****
	 *     深圳股票：00****或30****
	 *     container 接收容器
	 *     
	 * 其他：
	 *     doc: http://blog.csdn.net/xp5xp6/article/details/53121481
	 *     v1.0: url: e.g "http://biz.finance.sina.com.cn/stock/flash_hq/kline_data.php?symbol=sz000002&begin_date=20160101&end_date=21000101"
	 *     v2.0: url: e.g "http://quotes.money.163.com/service/chddata.html?code=0601857&start=20170705&end=20170809&fields=TCLOSE;HIGH;LOW;TOPEN;VOTURNOVER;"
	 
	 */
	public int getKLine(String id, int datalen, List<KLine> container)
	{
		int error = 0;
		
		String innerID = "";
		if((id.startsWith("60") || id.startsWith("68")) && 6 == id.length()) {
			// 600000 上海
			// 688716 科创
			innerID = "sh" + id;
		} else if((id.startsWith("00") ||  id.startsWith("30")) && 6 == id.length()) {
			// 000001 深圳
			// 300163 创业
			innerID = "sz" + id;
		} else if((id.startsWith("83") ||  id.startsWith("87") ||  id.startsWith("92")) && 6 == id.length()) {
			// 832522 873122 920682 北京
			innerID = "bj" + id;
		} else if(id.startsWith("999999")) {
			// 999999 上证指数
			innerID = "sh" + "000001";
		} else {
			error = -10;
			return error;
		}

		// http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sz002095&scale=240&ma=no&datalen=1023
		String urlStr = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?";
		urlStr = urlStr + "symbol=" + innerID + "&scale=240&ma=no&datalen=" + String.valueOf(datalen);
		
		try
		{
			String jsonStr = m_http.getWebData(urlStr, 1000);
			parseJson3(jsonStr, container);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception[WebStockDayK]:" + e.getMessage()); 
			error = -1;
        	return error;
		}
		
		Collections.sort(container, new Comparator<KLine>() {
            @Override
			public int compare(KLine o1, KLine o2) {
            	KLine sdto = o2;
    			int iRet = 0;
    			// date compare
    			if(null != o1.date && null != sdto.date)
    			{
    				iRet = o1.date.compareTo(sdto.date);
    				if(0 == iRet)
    				{
    					// time compare
    					if(null != o1.time && null != sdto.time)
    					{
    						return o1.time.compareTo(sdto.time);
    					}
    					else if(null != o1.time && null == sdto.time)
    					{
    						return 1;
    					}
    					else 
    					{
    						return 0;
    					}
    				}
    				else
    				{
    					return iRet;
    				}
    			}
    			else if(null != o1.date && null == sdto.date)
    			{
    				return 1;
    			}
    			else 
    			{
    				return 0;
    			}
            }
        });
		
		return error;
	}
	
	// for html: "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sz002095&scale=240&ma=no&datalen=1023"
	private static void parseHtml2(String in_str, List<KLine> resultList)
	{
		String[] lines = in_str.split("\n");
		for(int i=0; i<lines.length; i++)
		{
			String line = lines[i];
			
			String[] cols = line.split(",");
			String date = cols[0];
        	String open = cols[3];
        	String close = cols[4];
        	String low = cols[5];
        	String high = cols[6];
        	String volume = cols[7];
        	
        	if(cols.length < 5 
        			|| date.length()!="0000-00-00".length())
        	{
        		continue;
        	}
        	
        	KLine cKLine = new KLine();
        	cKLine.date = date;
        	cKLine.open = Double.parseDouble(open);
        	cKLine.close = Double.parseDouble(close);
        	cKLine.low = Double.parseDouble(low);
        	cKLine.high = Double.parseDouble(high);
        	cKLine.volume = Double.parseDouble(volume);
        	
        	if(0 == Double.compare(0.0f, cKLine.close))
        	{
        		continue;
        	}
        	
        	resultList.add(cKLine);
		}
	}
	
	// for html: "http://biz.finance.sina.com.cn/stock/flash_hq/kline_data.php?symbol=sz000002&begin_date=20160101&end_date=21000101"
	private static int parseHtml1(String in_str, List<KLine> resultList)
	{
		int error = 0;
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringReader sr = new StringReader(in_str);
	        InputSource is = new InputSource(sr);
	        Document doc = builder.parse(is);
	        Element rootElement = doc.getDocumentElement();
	        // 检查返回数据有效性
	        if(!rootElement.getTagName().contains("control")) 
	        {
	        	error = -30;
	        }

	        NodeList contents = rootElement.getElementsByTagName("content");
	        int lenList = contents.getLength();
	        for (int i = 0; i < lenList; i++) {
	        	KLine cKLine = new KLine();
	        	Node cnode = contents.item(i);
	        	String date = ((Element)cnode).getAttribute("d");
	        	String open = ((Element)cnode).getAttribute("o");
	        	String high = ((Element)cnode).getAttribute("h");
	        	String close = ((Element)cnode).getAttribute("c");
	        	String low = ((Element)cnode).getAttribute("l");
	        	String volume = ((Element)cnode).getAttribute("v");
	        	cKLine.date = date;
	        	cKLine.open = Double.parseDouble(open);
	        	cKLine.close = Double.parseDouble(close);
	        	cKLine.low = Double.parseDouble(low);
	        	cKLine.high = Double.parseDouble(high);
	        	cKLine.volume = Double.parseDouble(volume);
	        	
	        	resultList.add(cKLine);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return error;
	}

	// for html: "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sz002095&scale=240&ma=no&datalen=1023"
	private static void parseJson3(String in_str, List<KLine> resultList) {
		if (in_str == null || in_str.trim().isEmpty()) {
			return;
		}
		
		try {
			// Parse the JSON array directly
			JSONArray jsonArray = new JSONArray(in_str);
			
			// Iterate through each JSON object in the array
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				// Create new KLine object
				KLine kLine = new KLine();
				
				// Extract values from JSON object
				kLine.date = jsonObject.getString("day");
				kLine.open = jsonObject.getDouble("open");
				kLine.high = jsonObject.getDouble("high");
				kLine.low = jsonObject.getDouble("low");
				kLine.close = jsonObject.getDouble("close");
				kLine.volume = jsonObject.getDouble("volume");
				
				// Only add if close price is not zero and date exists
				if (kLine.date != null && !kLine.date.isEmpty() && 
					Double.compare(kLine.close, 0.0) != 0) {
					resultList.add(kLine);
				}
			}
		} catch (Exception e) {
			System.err.println("Error parsing JSON: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private CHttp m_http;
}
