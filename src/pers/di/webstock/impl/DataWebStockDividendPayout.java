package pers.di.webstock.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import pers.di.common.CLog;
import pers.di.common.CThread;
import pers.di.webstock.IWebStock.DividendPayout;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

public class DataWebStockDividendPayout  extends HttpHelper
{
	/*
	 * 从网络中获得某只股票的分红派息因子
	 * 
	 * 返回值：
	 *     返回0为成功，其他值为失败
	 * 参数：
	 *     container 接收容器
	 */
	public int getDividendPayout(String id, List<DividendPayout> container)
	{
		limitAccessSpeed(1500);
		
		int error = 0;
		
		if(null != container)
		{
			container.clear();
		}
		
		// e.g http://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/300163.phtml
		String urlStr = "http://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/";
		
		if(id.contains("999999")) 
		{
			error = 0;
			return error; // 上证指数没有分红派息
		}
		
		try{  
			urlStr = urlStr + id + ".phtml";
			
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
//			FileWriter fw = null; 
//			fw = new FileWriter("D:/test.txt");
//			fw.write(data);
//			fw.close();
	        
//			Parser myParser;     
//	        NodeList nodeList = null;     
//	        myParser =Parser.createParser(data, "utf-8");     
//	        NodeFilter tableFilter = new NodeClassFilter(TableTag.class);     
//	        OrFilter lastFilter = new OrFilter();     
//	        lastFilter.setPredicates(new NodeFilter[] { tableFilter });     
//	        nodeList =myParser.parse(lastFilter);
//	        System.out.println(nodeList.size());
//	        for (int i = 0; i <=nodeList.size(); i++) {
//	        	if (nodeList.elementAt(i) instanceof TableTag) {     
//	        		  
//                    TableTag tag = (TableTag)nodeList.elementAt(i);     
//                     
//                    Node cNodeFenHong = nodeList.elementAt(0);
//                    System.out.println(cNodeFenHong.getText());
////                     System.out.println(tag.getChildrenHTML());  
//                     System.out.println("-----------------------------------------------------");  
//	        	}
//	        }
			
	        Parser parser = Parser.createParser(data, "utf-8");
            TagNameFilter filter1 = new TagNameFilter("table");  
            NodeList tablelist = parser.parse(filter1); 
            Node cNodeFenHong = null;
            Node cNodePeiGu = null;
            //System.out.println(tablelist.size());
            for (int i = 0; i < tablelist.size(); i++) {  
                    Node cTmpNode = tablelist.elementAt(i);
                    if(cTmpNode.getText().contains("sharebonus_1"))
                    {
                    	cNodeFenHong = cTmpNode;
                    }
                    if(cTmpNode.getText().contains("sharebonus_2"))
                    {
                    	cNodePeiGu = cTmpNode;
                    }
	        }
            //System.out.println(cNodeFenHong.toHtml());
            //System.out.println(cNodePeiGu.toHtml());
            {
            	Parser parser2 = Parser.createParser(cNodeFenHong.toHtml(), "utf-8");
                TagNameFilter filter2 = new TagNameFilter("tr");
                NodeList tablelist2 = parser2.parse(filter2); 
                for (int i = 0; i < tablelist2.size(); i++) { 
                	if(i<3) continue;
                	DividendPayout cDividendPayout = new DividendPayout();
                	
                	Node cTmpNode = tablelist2.elementAt(i);
                	//System.out.println(cTmpNode.toHtml());
                	
                	Parser parser3 = Parser.createParser(cTmpNode.toHtml(), "utf-8");
                    TagNameFilter filter3 = new TagNameFilter("td");
                    NodeList tablelist3 = parser3.parse(filter3); 
                    for (int j = 0; j < tablelist3.size(); j++) {
                    	Node cTmpNodecol = tablelist3.elementAt(j);
                    	String tmpStr = cTmpNodecol.toPlainTextString();
                    	//System.out.println(tmpStr);
                    	if(5 == j)
                    		cDividendPayout.date = tmpStr; // 除权除息日
                    	if(7 == j && cDividendPayout.date.length()<8)
                    		cDividendPayout.date = tmpStr; // 红股上市日
                    	if(1 == j)
                    		cDividendPayout.songGu = Double.parseDouble(tmpStr);
                    	if(2 == j)
                    		cDividendPayout.zhuanGu = Double.parseDouble(tmpStr);
                    	if(3 == j)
                    		cDividendPayout.paiXi = Double.parseDouble(tmpStr);
                    }
                    if(tablelist3.size() < 5) // 数据没有
                    {
                    	continue;
                    }
                    if(cDividendPayout.date.replace("-", "").trim().length()==0)
                    {
                    	continue;
                    }
                    container.add(cDividendPayout);
                    //System.out.println("--------------------------------");
                }
            }
            {
            	Parser parser2 = Parser.createParser(cNodePeiGu.toHtml(), "utf-8");
                TagNameFilter filter2 = new TagNameFilter("tr");
                NodeList tablelist2 = parser2.parse(filter2); 
                for (int i = 0; i < tablelist2.size(); i++) { 
                	if(i<2) continue;
                	DividendPayout cDividendPayout = new DividendPayout();
                	
                	Node cTmpNode = tablelist2.elementAt(i);
                	//System.out.println(cTmpNode.toHtml());
                	
                	Parser parser3 = Parser.createParser(cTmpNode.toHtml(), "utf-8");
                    TagNameFilter filter3 = new TagNameFilter("td");
                    NodeList tablelist3 = parser3.parse(filter3); 
                    if(tablelist3.size()>1) 
                    {
                    	//System.out.println("WebStockDividendPayout PeiGu");
                    }
                    for (int j = 0; j < tablelist3.size(); j++) {
                    	Node cTmpNodecol = tablelist3.elementAt(j);
                    	String tmpStr = cTmpNodecol.toPlainTextString();
                    	//System.out.println(tmpStr);
                    }
                    //System.out.println("--------------------------------");
                }
            }

        }catch (Exception e) {  
        	e.printStackTrace();
			System.out.println("Exception[WebStockDividendPayout]:" + e.getMessage()); 
			if(e.getMessage().contains("Server returned HTTP response code: 456 for URL:"))
			{
				CLog.error("DATAAPI", "Exception[WebStockDayDetail]:%s, need to wait!", e.getMessage());
				// 高频访问检查, 停止2分钟
				CThread.msleep(1000*60*6);
			}
			error = -1;
        	return error;
        }  
		
		Collections.sort(container, new Comparator<DividendPayout>() {
            @Override
			public int compare(DividendPayout o1, DividendPayout o2) {
                return o1.date.compareTo(o2.date);
            }
        });
		return error;
	}
	
    private  byte[] readInputStream(InputStream inputStream) throws IOException {    
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
