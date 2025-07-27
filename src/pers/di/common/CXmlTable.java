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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/*
 * Node store,
 *    Node could have property-value
 */
public class CXmlTable {
	
	public enum CALLBACKTYPE
	{
		INVALID,
		COMMITED,
	}
	
	public static interface ICallback
	{
		abstract public void onNotify(CALLBACKTYPE type);
	}

	public static class RowCursor 
	{
		public RowCursor()
		{
			m_node = null;
		}
		
		public Map<String,String> columesMap()
		{
			Map<String,String> columesMap = new HashMap<String,String>();
			NamedNodeMap attrs = ((Element)m_node).getAttributes();
			for(int i=0 ;i<attrs.getLength(); i++)
			{
				Attr attr = (Attr) attrs.item(i);
				String attrName = attr.getName();
				String attrValue = attr.getValue();
				columesMap.put(attrName, attrValue);
			}
			return columesMap;
		}
		
		public boolean setColume(String name, String value)
		{
			if(null == m_node)
			{
				return false;
			}
			((Element)m_node).setAttribute(name, value);
			return true;
		}

		public boolean hasColume(String name)
		{
			if(null == m_node)
			{
				return false;
			}
			return ((Element)m_node).hasAttribute(name);
		}
		public String getColume(String name)
		{
			if(null == m_node || !((Element)m_node).hasAttribute(name))
			{
				return null;
			}
			return ((Element)m_node).getAttribute(name);
		}

		public boolean valid()
		{
			return null == m_node?false:true;
		}
		
		private void setContent(NodeList nodeList, int index)
		{
			m_nodelist = nodeList;
			m_index = index;
			if(m_index >= 0 && m_index < m_nodelist.getLength())
			{
				m_node = nodeList.item(m_index);
			}
			else
			{
				m_node = null;
			}
		}
		private void moveNext()
		{
			m_index = m_index+1;
			if(m_index < m_nodelist.getLength())
			{
				m_node = m_nodelist.item(m_index);
			}
			else
			{
				m_nodelist = null;
				m_index = -1;
				m_node = null;
			}
		}
		
		private NodeList m_nodelist;
		private int m_index;
		private Node m_node;
	}
	
	public CXmlTable(String fileName)
	{
		m_fileName = fileName;
		m_file = null;
		m_doc = null;
		m_ICallback = null;
		m_rowCursor = new RowCursor();
	}
	
	public void registerCallback(ICallback cb)
	{
		m_ICallback = cb;
	}
	
	public boolean open()
	{
		m_file = new File(m_fileName);
		if(!m_file.exists())
		{
			m_doc=null;
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder= factory.newDocumentBuilder();
				m_doc=builder.newDocument();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Element root=m_doc.createElement("table");
			m_doc.appendChild(root);
	        
	        TransformerFactory tfactory=TransformerFactory.newInstance();
			Transformer transformer = null;
			try {
				transformer = tfactory.newTransformer();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

			// 获得最终oriXmlStr
			String oriXmlStr ="";
			if(null != m_doc && null != transformer)
			{
				transformer.setOutputProperty("encoding","GBK");
				DOMSource source=new DOMSource(m_doc);
				
				StringWriter writer = new StringWriter();
				StreamResult result=new StreamResult(writer);
				try {
					transformer.transform(source,result);
					oriXmlStr = writer.toString();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
			
			// 格式化XmlStr
			String formatedXmlStr = "";
			try {
				formatedXmlStr = CUtilsXML.format(oriXmlStr);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
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
				return false;
			}
		}
		else
		{
			try
			{
				String xmlStr = "";
				BufferedReader reader = new BufferedReader(new FileReader(m_file));
		        int fileLen = (int)m_file.length();
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
					return false; // 没有内容 load失败
				}
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    StringReader sr = new StringReader(xmlStr);
			    InputSource is = new InputSource(sr);
			    m_doc = builder.parse(is);
			    Element root = m_doc.getDocumentElement();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println(e.getMessage()); 
				return false;
			}
		}
		return true;
	}

	public boolean commit()
	{
		TransformerFactory tfactory=TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tfactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		// 获得最终oriXmlStr
		String oriXmlStr ="";
		if(null != m_doc && null != transformer)
		{
			transformer.setOutputProperty("encoding","GBK");
			DOMSource source=new DOMSource(m_doc);
			
			StringWriter writer = new StringWriter();
			StreamResult result=new StreamResult(writer);
			try {
				transformer.transform(source,result);
				oriXmlStr = writer.toString();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		// 格式化XmlStr
		String formatedXmlStr = "";
		try {
			formatedXmlStr = CUtilsXML.format(oriXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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
			return false;
		}
		
		if(null != m_ICallback)
		{
			m_ICallback.onNotify(CALLBACKTYPE.COMMITED);
		}
		return true;
	}
	
	public int size()
	{
		if(null == m_doc)
		{
			return 0;
		}
		Element root = m_doc.getDocumentElement();
		if(null == root)
		{
			return 0;
		}
		NodeList nodeList =  root.getElementsByTagName("row");
		if(null == nodeList)
		{
			return 0;
		}
		return nodeList.getLength();
	}
	
	public RowCursor addRow()
	{
		if(null == m_doc)
		{
			return null;
		}
		Element root = m_doc.getDocumentElement();
		if(null == root)
		{
			return null;
		}

		Element rowNode = m_doc.createElement("row");
		root.appendChild(rowNode);
        
		NodeList nodeList =  root.getElementsByTagName("row");
		m_rowCursor.setContent(nodeList, nodeList.getLength()-1);
		
		return m_rowCursor;
	}
	
	public boolean deleteRow(RowCursor cursor)
	{
		if(null == m_doc || null == cursor ||  null == cursor.m_node)
		{
			return false;
		}
		Element root = m_doc.getDocumentElement();
		if(null == root)
		{
			return false;
		}
		root.removeChild(cursor.m_node);
		
		m_rowCursor.setContent(null, -1);
		
		return true;
	}
	
	public boolean deleteAll()
	{
		if(null == m_doc)
		{
			return false;
		}
		Element root = m_doc.getDocumentElement();
		if(null == root)
		{
			return false;
		}
		m_doc.removeChild(root);
		
		root=m_doc.createElement("table");
		m_doc.appendChild(root);
		
		m_rowCursor.setContent(null, -1);
		
		return false;
	}
	
	public RowCursor moveFirst()
	{
		if(null == m_doc)
		{
			return null;
		}
		Element root = m_doc.getDocumentElement();
		if(null == root)
		{
			return null;
		}
		
		NodeList nodeList =  root.getElementsByTagName("row");
		if(null != nodeList && nodeList.getLength() > 0)
		{
			m_rowCursor.setContent(nodeList, 0);
			return m_rowCursor;
		}
		else
		{
			return null;
		}
	}
	
	public RowCursor moveNext()
	{
		m_rowCursor.moveNext();
		if(null != m_rowCursor.m_node)
		{
			return m_rowCursor;
		}
		else
		{
			return null;
		}
	}
	
	private String m_fileName;
	private File m_file;
	private Document m_doc;
	private ICallback m_ICallback;
	private RowCursor m_rowCursor;
}
