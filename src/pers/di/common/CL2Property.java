package pers.di.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

/**
 * Level 2 property
 * Map<String, Map<String, String>>
 * 
 * @author wudi
 *
 */
public class CL2Property {

	public void setPropertyExBoolean(String property, String subProperty, boolean value)
	{
		this.setProperty(property, subProperty, String.format("%b", value));
	}
	public Boolean getPropertyExBoolean(String property, String subProperty)
	{
		Boolean value = null;
		String sVal = this.getProperty(property, subProperty);
		if(null != sVal)
		{
			value = Boolean.parseBoolean(sVal);
		}
		return value;
	}
	
	public void setPropertyExLong(String property, String subProperty, long value)
	{
		this.setProperty(property, subProperty, String.format("%d", value));
	}
	public Long getPropertyExLong(String property, String subProperty)
	{
		Long value = null;
		String sVal = this.getProperty(property, subProperty);
		if(null != sVal)
		{
			value = Long.parseLong(sVal);
		}
		return value;
	}
	
	public void setPropertyExDouble(String property, String subProperty, double value)
	{
		this.setProperty(property, subProperty, String.format("%.3f", value));
	}
	public Double getPropertyExDouble(String property, String subProperty)
	{
		Double value = null;
		String sVal = this.getProperty(property, subProperty);
		if(null != sVal)
		{
			value = Double.parseDouble(sVal);
		}
		return value;
	}
	

	/*
	 * ****************************************************************************************************
	 */
	
	public CL2Property(String storeFileName)
	{
		m_fileName = storeFileName;
		m_L2PropMap = new TreeMap<String, Map<String,String>>();
	}
	
	public void setProperty(String property, String subProperty, String value)
	{
		if(m_L2PropMap.containsKey(property))
		{
			Map<String,String> subPropMap = m_L2PropMap.get(property);
			subPropMap.put(subProperty, value);
			
		}
		else
		{
			Map<String,String> subPropMap = new TreeMap<String,String>();
			subPropMap.put(subProperty, value);
			m_L2PropMap.put(property, subPropMap);
		}
	}
	
	public String getProperty(String property, String subProperty)
	{
		if(m_L2PropMap.containsKey(property))
		{
			Map<String,String> subPropMap = m_L2PropMap.get(property);
			if(null != subPropMap && subPropMap.containsKey(subProperty))
			{
				return subPropMap.get(subProperty);
			}
		}
		return null;
	}
	
	public int clear()
	{
		m_L2PropMap.clear();
		return 0;
	}
	
	public int clear(String mainProperty)
	{
		if(m_L2PropMap.containsKey(mainProperty))
		{
			m_L2PropMap.remove(mainProperty);
		}
		return 0;
	}
	
	public int clear(String mainProperty, String subProperty)
	{
		if(m_L2PropMap.containsKey(mainProperty))
		{
			Map<String, String> subMap = m_L2PropMap.get(mainProperty);
			if(null != subMap && subMap.containsKey(subProperty))
			{
				subMap.remove(subProperty);
			}
		}
		return 0;
	}
	
	public boolean contains(String mainProperty)
	{
		return m_L2PropMap.containsKey(mainProperty);
	}
	public boolean contains(String mainProperty, String subProperty)
	{
		if(m_L2PropMap.containsKey(mainProperty))
		{
			Map<String, String> subMap = m_L2PropMap.get(mainProperty);
			if(null != subMap)
			{
				return subMap.containsKey(subProperty);
			}
		}
		return false;
	}
	
	public int size()
	{
		return m_L2PropMap.size();
	}
	
	public int size(String mainProperty)
	{
		if(m_L2PropMap.containsKey(mainProperty))
		{
			Map<String, String> subMap = m_L2PropMap.get(mainProperty);
			if(null != subMap)
			{
				return subMap.size();
			}
		}
		return 0;
	}
	
	public List<String> list()
	{
		List<String> listProperty = new ArrayList<String>();
		for (Map.Entry<String, Map<String, String>> entryTop : m_L2PropMap.entrySet()) {  
			listProperty.add(entryTop.getKey());
        }  
		return listProperty;
	}
	public List<String> list(String mainProperty)
	{
		List<String> listProperty = new ArrayList<String>();
		for (Map.Entry<String, Map<String, String>> entryTop : m_L2PropMap.entrySet()) {  
			if(entryTop.getKey().equals(mainProperty))
			{
				Map<String, String> subMap = entryTop.getValue();
				for (Map.Entry<String, String> entrySub : subMap.entrySet()) {  
					listProperty.add(entrySub.getKey());
				}
			}
		} 
		return listProperty;
	}

	public int sync2file()
	{
		//System.out.println("sync2file");  
		 
		File cfile=new File(m_fileName);
		if(cfile.exists())
		{
			cfile.delete();
		}
		
		Document doc=null;
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder= factory.newDocumentBuilder();
			doc=builder.newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Element root=doc.createElement("CL2Property");
        doc.appendChild(root);
        
        for (Map.Entry<String, Map<String, String>> entryTop : m_L2PropMap.entrySet()) {  
          
        	String mainProperty = entryTop.getKey();
            //System.out.println("Key = " + mainProperty);  
            
            Element NodeMainProperty =doc.createElement("MainProperty");
            NodeMainProperty.setAttribute("property", mainProperty);
            
        	root.appendChild(NodeMainProperty);
          
            Map<String, String> subMap = entryTop.getValue();
            for (Map.Entry<String, String> entrySub : subMap.entrySet()) {  
            	
            	String subProperty = entrySub.getKey();
            	String value = entrySub.getValue();
            	//System.out.println("    Key=" + subProperty + " value=" + value);  
            	
            	Element NodeSubProperty =doc.createElement("SubProperty");
            	NodeSubProperty.setAttribute("property", subProperty);
            	NodeSubProperty.setAttribute("value", value);
            	
            	NodeMainProperty.appendChild(NodeSubProperty);
            }
        }  
        
        TransformerFactory tfactory=TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tfactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
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
				return -1;
			}
		}
		
		// 格式化XmlStr
		String formatedXmlStr = "";
		try {
			formatedXmlStr = CUtilsXML.format(oriXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		// 更新到文件
		File cfile_new = new File(m_fileName);
		try {
			FileWriter fw = new FileWriter(cfile_new.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(formatedXmlStr);
		    bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	public int sync2mem()
	{
		//System.out.println("sync2mem"); 
		
		m_L2PropMap.clear();
		
		String xmlStr = "";
		File cfile=new File(m_fileName);
		if(!cfile.exists())
		{
			return -1; // 没有文件 load失败
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
				return -1; // 没有内容 load失败
			}
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    StringReader sr = new StringReader(xmlStr);
		    InputSource is = new InputSource(sr);
		    Document doc = builder.parse(is);
		    Element rootElement = doc.getDocumentElement();
		    
		    if(!rootElement.getTagName().contains("CL2Property")) 
			{
				return -1; // 没有root load失败
			}
		    
		    NodeList nodelist_MainProperty = rootElement.getElementsByTagName("MainProperty");
		    for (int iMain = 0; iMain < nodelist_MainProperty.getLength(); iMain++) {
	        	Node node_MainProperty = nodelist_MainProperty.item(iMain);
	        	if(node_MainProperty.getNodeType() == Node.ELEMENT_NODE)
	        	{
	        		String mainProperty = ((Element)node_MainProperty).getAttribute("property");
	        		//System.out.println("mainProperty=" + mainProperty); 
	        		
	        		Map<String, String> subPropMap = new TreeMap<String, String>();
	        		m_L2PropMap.put(mainProperty, subPropMap);
	        		
	        		NodeList nodelist_SubProperty = node_MainProperty.getChildNodes();
	        		for (int iSub = 0; iSub < nodelist_SubProperty.getLength(); iSub++) {
	        			Node node_SubProperty = nodelist_SubProperty.item(iSub);
	        			if(node_SubProperty.getNodeType() == Node.ELEMENT_NODE)
	        			{
	        				String subProperty = ((Element)node_SubProperty).getAttribute("property");
	        				String value = ((Element)node_SubProperty).getAttribute("value");
	        				//System.out.println("    subProperty=" + subProperty + " value=" + value); 
	        				
	        				subPropMap.put(subProperty, value);
	        			}
	        		}
	        	}
		    }
		    
		    
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			return -1;
		}
		
		return 0;
	}
	
	private void dump()
	{
        for (Map.Entry<String, Map<String, String>> entryTop : m_L2PropMap.entrySet()) {  
          
            System.out.println("Key = " + entryTop.getKey());  
          
            Map<String, String> subMap = entryTop.getValue();
            for (Map.Entry<String, String> entrySub : subMap.entrySet()) {  
            	System.out.println("    Key = " + entrySub.getKey());  
            }
        }  
	}
	
	private String m_fileName;
	private Map<String, Map<String, String>> m_L2PropMap;
}
