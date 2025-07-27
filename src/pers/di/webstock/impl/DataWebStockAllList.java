package pers.di.webstock.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import pers.di.webstock.IWebStock.StockItem;



public class DataWebStockAllList extends HttpHelper 
{
	/*
	 * 从网络中获得所有股票项目
	 * 
	 * 返回值：
	 *     返回0为成功，其他值为失败
	 * 参数：
	 *     container 接收容器
	 */
	public int getAllStockList(List<StockItem> container)
	{
		limitAccessSpeed(2000);
		
		int error = 0;
		
		if(null == container) return 0;
		
		try{  
			String allStockListUrl = "http://quote.eastmoney.com/stock_list.html";
//            URL url = new URL(allStockListUrl);  
//            HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
//            InputStream inputStream = conn.getInputStream(); 
//            byte[] getData = readInputStream(inputStream); 
//            String data = new String(getData, "gbk");  
//            System.out.println(data);
//            FileWriter fw = null; 
//            fw = new FileWriter("D:/add2.txt");
//            fw.write(data);
//            fw.close();
            
			URL url = new URL(allStockListUrl);    
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();    

	        conn.setConnectTimeout(5*1000);  //设置连接超时间 
	        conn.setReadTimeout(15*1000); //设置读取超时时间
	        
	        //防止屏蔽程序抓取而返回403错误  
	        conn.setRequestProperty("User-Agent", getRandomUserAgent());  
	        
            Parser parser = new Parser(conn); 
            parser.setEncoding("gbk");
            TagNameFilter filter1 = new TagNameFilter("DIV");  
            NodeList list1 = parser.parse(filter1);  
            
            // find qox node
            Node cQoxNode = null;
            if(list1!=null){  
                //System.out.println("list.size()==="+list.size());  
                for(int i=0; i<list1.size(); i++)  
                {
                	Node cNode = list1.elementAt(i);
                	if(cNode.getText().contains("class=\"qox\""))
                	{
                		cQoxNode = cNode;
                		break;
                	}
                }
            }  
            
			 TagNameFilter filter2=new TagNameFilter ("li");  
	         Parser p = Parser.createParser(cQoxNode.toHtml(), "gbk");
			 NodeList list2=p.extractAllNodesThatMatch(filter2);
 					
			 int allCount=0;
             for(int i=0;i<list2.size();i++)  
             {
             	Node cNode = list2.elementAt(i);
             	String tmpStr = cNode.toPlainTextString();
             	int il = tmpStr.indexOf("(");
             	int ir = tmpStr.indexOf(")");
             	String name = tmpStr.substring(0, il);
             	String id = tmpStr.substring(il+1,ir);
             	if(id.startsWith("60") || id.startsWith("00") || id.startsWith("30"))
             	{
             		if(id.length() == 6)
             		{
                 		// System.out.println(name + "," + id);
                 		allCount++;
                 		StockItem cStockItem = new StockItem();
                 		cStockItem.name = name;
                 		cStockItem.ID = id;
                 		container.add(cStockItem);
             		}
             	}
             }
             // System.out.println(allCount); 
             
             if(container.size() <= 0) 
        	 {
            	 error = -30;
        	 }

        }catch (Exception e) {  
        	e.printStackTrace();
        	System.out.println("Exception[WebStockAllList]:" + e.getMessage()); 
        	error = -1;
        }  
		return error;
	}


//	private static String ENCODE = "GBK";
//    private static void message( String szMsg ) {
//        try{ 
//        	System.out.println(new String(szMsg.getBytes(ENCODE), System.getProperty("file.encoding"))); 
//        	Write(new String(szMsg.getBytes(ENCODE), System.getProperty("file.encoding")));
//        	Write("\n");
//        	}    
//        catch(Exception e )
//        {}
//    }
//    public static  byte[] readInputStream(InputStream inputStream) throws IOException {    
//        byte[] buffer = new byte[1024];    
//        int len = 0;    
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();    
//        while((len = inputStream.read(buffer)) != -1) {    
//            bos.write(buffer, 0, len);    
//        }    
//        bos.close();    
//        return bos.toByteArray();    
//    }    
//    public static void Write(String data) throws IOException
//    {
//        FileWriter fw = null; 
//        fw = new FileWriter("D:/add2xxx.txt", true);
//        fw.write(data);
//        fw.close();
//    }
	
}
