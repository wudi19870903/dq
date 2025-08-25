package pers.di.account.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import pers.di.account.*;
import pers.di.common.*;

public class AccountStore {
	
	public static class StoreEntity
	{
		public StoreEntity()
		{
			date = CUtilsDateTime.GetCurDateStr();
		    time = CUtilsDateTime.GetCurTimeStr();
		    money = 0.0f;
		    commissionOrderList = new ArrayList<CommissionOrder>();
		    dealOrderList = new ArrayList<DealOrder>();
		    holdStockList = new ArrayList<HoldStock>();
		}
		public void reset(double fInitMoney)
		{
			date = CUtilsDateTime.GetCurDateStr();
		    time = CUtilsDateTime.GetCurTimeStr();
		    money = fInitMoney;
		    commissionOrderList.clear();
		    dealOrderList.clear();
		    holdStockList.clear();
		}
		public String date;
		public String time;
		public double money;
		public List<CommissionOrder> commissionOrderList;
		public List<DealOrder> dealOrderList;
		public List<HoldStock> holdStockList;
	}
	
	public AccountStore(String dataRoot, String accountID)
	{
		// init root dir
		if(null != dataRoot)
		{
			s_accountDataRoot = dataRoot;
		}
		if(!CFileSystem.isDirExist(s_accountDataRoot))
		{
			CFileSystem.createDir(s_accountDataRoot);
		}
		
		m_accountID = accountID;
		m_accXMLFile = s_accountDataRoot + "\\" + m_accountID + ".xml";
		m_storeEntity = new StoreEntity();
	}
	
	public String accountID()
	{
		return m_accountID;
	}
	
	public StoreEntity storeEntity()
	{
		return m_storeEntity;
	}

	public boolean storeInit()
	{
        m_storeEntity.reset(0.0f);
		return sync2File();
	}
	
	public boolean sync2File()
	{
		Document doc=null;
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder= factory.newDocumentBuilder();
			doc=builder.newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 创建元素
		Element root=doc.createElement("account");
		root.setAttribute("ID", m_accountID);
        doc.appendChild(root);
        
        if(null != m_storeEntity)
        {
        	// date time
        	if(null != m_storeEntity.date)
        	{
        		root.setAttribute("date", m_storeEntity.date);
        	}
        	if(null != m_storeEntity.time)
        	{
        		root.setAttribute("time", m_storeEntity.time);
        	}
        	
        	// money
        	{
        		Element Node_Money = doc.createElement("Money");
        		root.appendChild(Node_Money);
        		String money =String.format("%.3f", m_storeEntity.money);
        		Node_Money.setAttribute("value", money);
        	}

        	// CommissionOrderList
        	if(null != m_storeEntity.commissionOrderList)
        	{
        		Element Node_CommissionOrderList=doc.createElement("commissionOrderList");
            	root.appendChild(Node_CommissionOrderList);
            	for(int i=0;i<m_storeEntity.commissionOrderList.size();i++)
            	{
            		CommissionOrder cCommissionOrder = m_storeEntity.commissionOrderList.get(i);
            		String tranactVal = "";
            		if(cCommissionOrder.tranAct == TRANACT.BUY) tranactVal= "BUY";
            		if(cCommissionOrder.tranAct == TRANACT.SELL) tranactVal= "SELL";
            		String amountVal = String.format("%d", cCommissionOrder.amount);
            		String priceVal =String.format("%.3f", cCommissionOrder.price);
            		String dealAmountVal =String.format("%d", cCommissionOrder.dealAmount);
            				
            		Element Node_commissionOrder = doc.createElement("commissionOrder");
            		Node_commissionOrder.setAttribute("date", cCommissionOrder.date);
            		Node_commissionOrder.setAttribute("time", cCommissionOrder.time);
            		Node_commissionOrder.setAttribute("tranAct", tranactVal);
            		Node_commissionOrder.setAttribute("stockID", cCommissionOrder.stockID);
            		Node_commissionOrder.setAttribute("amount", amountVal);
            		Node_commissionOrder.setAttribute("price", priceVal);
            		Node_commissionOrder.setAttribute("dealAmount", dealAmountVal);
            		
            		Node_CommissionOrderList.appendChild(Node_commissionOrder);
            	}
        	}
        	
        	// dealOrderList
        	if(null != m_storeEntity.dealOrderList)
        	{
        		Element Node_DealOrderList=doc.createElement("dealOrderList");
            	root.appendChild(Node_DealOrderList);
            	for(int i=0;i<m_storeEntity.dealOrderList.size();i++)
            	{
            		DealOrder cDealOrder = m_storeEntity.dealOrderList.get(i);
            		String tranactVal = "";
            		if(cDealOrder.tranAct == TRANACT.BUY) tranactVal= "BUY";
            		if(cDealOrder.tranAct == TRANACT.SELL) tranactVal= "SELL";
            		String amountVal = String.format("%d", cDealOrder.amount);
            		String priceVal =String.format("%.3f", cDealOrder.price);
            		String costVal =String.format("%.3f", cDealOrder.cost);
            				
            		Element Node_dealOrder = doc.createElement("dealOrder");
            		Node_dealOrder.setAttribute("date", cDealOrder.date);
            		Node_dealOrder.setAttribute("time", cDealOrder.time);
            		Node_dealOrder.setAttribute("tranAct", tranactVal);
            		Node_dealOrder.setAttribute("stockID", cDealOrder.stockID);
            		Node_dealOrder.setAttribute("amount", amountVal);
            		Node_dealOrder.setAttribute("price", priceVal);
            		Node_dealOrder.setAttribute("cost", costVal);
            		Node_DealOrderList.appendChild(Node_dealOrder);
            	}
        	}
        	
        	// holdStockList
        	if(null != m_storeEntity.holdStockList)
        	{
        		Element Node_holdStockList=doc.createElement("holdStockList");
            	root.appendChild(Node_holdStockList);
            	for(int i=0;i<m_storeEntity.holdStockList.size();i++)
            	{
            		HoldStock cHoldStock = m_storeEntity.holdStockList.get(i);
  
            		String totalAmount = String.format("%d", cHoldStock.totalAmount);
            		String availableAmount = String.format("%d", cHoldStock.availableAmount);
            		String curPrice =String.format("%.3f", cHoldStock.curPrice);
            		String refPrimeCostPrice =String.format("%.3f", cHoldStock.refPrimeCostPrice);
            				
            		Element Node_holdStock = doc.createElement("holdStock");
            		Node_holdStock.setAttribute("createDate", cHoldStock.createDate);
            		Node_holdStock.setAttribute("stockID", cHoldStock.stockID);
            		Node_holdStock.setAttribute("totalAmount", totalAmount);
            		Node_holdStock.setAttribute("availableAmount", availableAmount);
            		Node_holdStock.setAttribute("curPrice", curPrice);
            		Node_holdStock.setAttribute("refPrimeCostPrice", refPrimeCostPrice);
            		
            		Node_holdStockList.appendChild(Node_holdStock);
            	}
        	}
        	
        }
		
		TransformerFactory tfactory=TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tfactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 获得最终oriXmlStr
		String oriXmlStr ="";
		if(null != doc && null != transformer)
		{
			transformer.setOutputProperty("encoding","GBK");
			DOMSource source=new DOMSource(doc);
			
			StringWriter writer = new StringWriter();
			StreamResult result=new StreamResult(writer);
			try {
				transformer.transform(source,result);
				oriXmlStr = writer.toString();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// 格式化XmlStr
		String formatedXmlStr = "";
		try {
			formatedXmlStr = CUtilsXML.format(oriXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 判断变化试图更新到文件
		boolean bNeedRewriteFile = false;
		File cfile=new File(m_accXMLFile);
		if(cfile.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(cfile));
		        int fileLen = (int)cfile.length();
		        char[] chars = new char[fileLen];
		        reader.read(chars);
		        reader.close();
		        String oldXmlStr = String.valueOf(chars);
		        if(!oldXmlStr.equals(formatedXmlStr))
		        {
		        	bNeedRewriteFile = true;
		        }
			}
	        catch(Exception e)
			{
				e.printStackTrace();
			}
		} else {
			bNeedRewriteFile = true;
		}
		
		if(bNeedRewriteFile)
		{
			if(cfile.exists())
			{
				cfile.delete();
			}
			
			// 更新到文件
			File cfile_new = new File(m_accXMLFile);
			try {
				FileWriter fw = new FileWriter(cfile_new.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(formatedXmlStr);
			    bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	public boolean sync2Mem()
	{
		String xmlStr = "";
		File cfile=new File(m_accXMLFile);
		if(!cfile.exists())
		{
			CLog.debug("ACCOUNT", "AccountStore storeInit (no file)\n");
			return false; // 没有文件 load失败
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
	        int fileLen = (int)cfile.length();
	        char[] chars = new char[fileLen];
	        reader.read(chars);
	        xmlStr = String.valueOf(chars);
//			String tempString = "";
//			while ((tempString = reader.readLine()) != null) {
//				xmlStr = xmlStr + tempString + "\n";
//	        }
			reader.close();
			//fmt.format("XML:\n" + xmlStr);
			if(xmlStr.length()<=0)
			{
				CLog.debug("ACCOUNT", "AccountStore storeInit (no content)\n");
				return false; // 没有内容 load失败
			}
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    StringReader sr = new StringReader(xmlStr);
		    InputSource is = new InputSource(sr);
		    Document doc = builder.parse(is);
		    Element rootElement = doc.getDocumentElement();
		    
		    // 检查返回数据有效性
		    if(!rootElement.getTagName().contains("account")) 
			{
		    	CLog.debug("ACCOUNT", "AccountStore storeInit (no account root)\n");
				return false; // 没有root load失败
			}
		    
        	// date time
		    String accDate = rootElement.getAttribute("date");
		    String accTime = rootElement.getAttribute("time");
		    
		    // money
		    Double money = 0.0;
		    {
		    	NodeList nodelist_Money = rootElement.getElementsByTagName("Money");
		    	if(nodelist_Money.getLength() == 1)
	        	{
		        	Node Node_Money = nodelist_Money.item(0);
		        	String value = ((Element)Node_Money).getAttribute("value");
		        	money = Double.parseDouble(value);
	        	}
		    }

		    // 委托单加载
		    List<CommissionOrder> commissionOrderList = new ArrayList<CommissionOrder>();
		    {
		    	NodeList nodelist_CommissionOrderList = rootElement.getElementsByTagName("commissionOrderList");
		        if(nodelist_CommissionOrderList.getLength() == 1)
	        	{
		        	Node Node_CommissionOrderList = nodelist_CommissionOrderList.item(0);
		        	NodeList nodelist_commissionOrder = Node_CommissionOrderList.getChildNodes();
			        for (int i = 0; i < nodelist_commissionOrder.getLength(); i++) {
			        	Node node_commissionOrder = nodelist_commissionOrder.item(i);
			        	if(node_commissionOrder.getNodeType() == Node.ELEMENT_NODE)
			        	{
			        		String date = ((Element)node_commissionOrder).getAttribute("date");
			        		String time = ((Element)node_commissionOrder).getAttribute("time");
				        	String tranAct = ((Element)node_commissionOrder).getAttribute("tranAct");
				        	String stockID = ((Element)node_commissionOrder).getAttribute("stockID");
				        	String amount = ((Element)node_commissionOrder).getAttribute("amount");
				        	String price = ((Element)node_commissionOrder).getAttribute("price");
				        	String dealAmount = ((Element)node_commissionOrder).getAttribute("dealAmount");
				        	
				        	CommissionOrder cCommissionOrder = new CommissionOrder();
				        	cCommissionOrder.date = date;
				        	cCommissionOrder.time = time;
				        	if(tranAct.equals("BUY")) cCommissionOrder.tranAct = TRANACT.BUY;
				        	if(tranAct.equals("SELL")) cCommissionOrder.tranAct = TRANACT.SELL;
				        	cCommissionOrder.stockID = stockID;
				        	cCommissionOrder.amount = Integer.parseInt(amount);
				        	cCommissionOrder.price = Double.parseDouble(price);
				        	cCommissionOrder.dealAmount = Integer.parseInt(dealAmount);
				        	commissionOrderList.add(cCommissionOrder);
			        	}
			        }
	        	}
		    }
		    
		    // dealOrder
		    List<DealOrder> dealOrderList = new ArrayList<DealOrder>();
		    {
		    	NodeList nodelist_dealOrderList = rootElement.getElementsByTagName("dealOrderList");
		        if(nodelist_dealOrderList.getLength() == 1)
	        	{
		        	Node Node_DealOrderList = nodelist_dealOrderList.item(0);
		        	NodeList nodelist_dealOrder = Node_DealOrderList.getChildNodes();
			        for (int i = 0; i < nodelist_dealOrder.getLength(); i++) {
			        	Node node_dealOrder = nodelist_dealOrder.item(i);
			        	if(node_dealOrder.getNodeType() == Node.ELEMENT_NODE)
			        	{
			        		String date = ((Element)node_dealOrder).getAttribute("date");
			        		String time = ((Element)node_dealOrder).getAttribute("time");
				        	String tranAct = ((Element)node_dealOrder).getAttribute("tranAct");
				        	String stockID = ((Element)node_dealOrder).getAttribute("stockID");
				        	String amount = ((Element)node_dealOrder).getAttribute("amount");
				        	String price = ((Element)node_dealOrder).getAttribute("price");
				        	String cost = ((Element)node_dealOrder).getAttribute("cost");
				        	
				        	DealOrder cDealOrder = new DealOrder();
				        	cDealOrder.date = date;
				        	cDealOrder.time = time;
				        	if(tranAct.equals("BUY")) cDealOrder.tranAct = TRANACT.BUY;
				        	if(tranAct.equals("SELL")) cDealOrder.tranAct = TRANACT.SELL;
				        	cDealOrder.stockID = stockID;
				        	cDealOrder.amount = Integer.parseInt(amount);
				        	cDealOrder.price = Double.parseDouble(price);
				        	cDealOrder.cost = Double.parseDouble(cost);
				        	dealOrderList.add(cDealOrder);
			        	}
			        }
	        	}
		    }
		    
		    // holdstock
		    List<HoldStock> holdStockList = new ArrayList<HoldStock>();
		    {
		    	NodeList nodelist_holdStockList = rootElement.getElementsByTagName("holdStockList");
		        if(nodelist_holdStockList.getLength() == 1)
	        	{
		        	Node Node_holdStockList = nodelist_holdStockList.item(0);
		        	NodeList nodelist_holdStock = Node_holdStockList.getChildNodes();
			        for (int i = 0; i < nodelist_holdStock.getLength(); i++) {
			        	Node node_holdStock = nodelist_holdStock.item(i);
			        	if(node_holdStock.getNodeType() == Node.ELEMENT_NODE)
			        	{
			        		String createDate = ((Element)node_holdStock).getAttribute("createDate");
				        	String stockID = ((Element)node_holdStock).getAttribute("stockID");
				        	String totalAmount = ((Element)node_holdStock).getAttribute("totalAmount");
				        	String availableAmount = ((Element)node_holdStock).getAttribute("availableAmount");
				        	String curPrice = ((Element)node_holdStock).getAttribute("curPrice");
				        	String refPrimeCostPrice = ((Element)node_holdStock).getAttribute("refPrimeCostPrice");
				        	
				        	HoldStock cHoldStock = new HoldStock();
				        	cHoldStock.createDate = createDate;
				        	cHoldStock.stockID = stockID;
				        	cHoldStock.totalAmount = Integer.parseInt(totalAmount);
				        	cHoldStock.availableAmount = Integer.parseInt(availableAmount);
				        	cHoldStock.curPrice = Double.parseDouble(curPrice);
				        	cHoldStock.refPrimeCostPrice = Double.parseDouble(refPrimeCostPrice);
				        	holdStockList.add(cHoldStock);
			        	}
			        }
	        	}
		    }
		    
		    m_storeEntity.date = accDate;
		    m_storeEntity.time = accTime;
		    m_storeEntity.money = money;
		    m_storeEntity.commissionOrderList = commissionOrderList;
		    m_storeEntity.dealOrderList = dealOrderList;
		    m_storeEntity.holdStockList = holdStockList;
		    return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			return false;
		}
	}
	
	/**
	 * 成员-----------------------------------------------------------------------
	 */
	private static String s_accountDataRoot = "rw\\account";
	
	private String m_accountID;
	private String m_accXMLFile;
	private StoreEntity m_storeEntity;
}
