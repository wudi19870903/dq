package pers.di.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import pers.di.common.CQThread.CQThreadRequest;

public class CLog {
	
	static enum LOGLEVEL {
		ERROR,
		WARNING,
		INFO,
		DEBUG,
	};

	/*
	 * LogOutRequest
	 */
	private static class LogOutRequest extends CQThreadRequest 
	{
		public LogOutRequest(String datetime, LOGLEVEL loglevel, String logtag, String logbuf)
		{
			m_datetime = datetime;
			m_loglevel = loglevel;
			m_logTag = logtag;
			m_logbuf = logbuf;
		}
		@Override
		public void doAction() {
			// TODO Auto-generated method stub
			CLog.implLogOutput(m_datetime, m_loglevel, m_logTag, m_logbuf); 
		}
		private String m_datetime;
		private LOGLEVEL m_loglevel;
		private String m_logTag;
		private String m_logbuf;
	}
	
	public static class LogContentCache
	{
		public LogContentCache()
		{
			m_logContent = "";
			s_syncObj = new CSyncObj();
		}
		public void lock()
		{
			s_syncObj.Lock();
		}
		public void unlock()
		{
			s_syncObj.UnLock();
		}
		public int size()
		{
			return m_logContent.length();
		}
		public String popContent()
		{
			String tmp = m_logContent;
			m_logContent = "";
			return tmp;
		}
		public void addContent(String content)
		{
			m_logContent += content;
		}
		private String m_logContent;
		private CSyncObj s_syncObj;
	}
	
	/*
	 * log config monitor, thread
	 */
	private static class LogConfigMonitorThread extends CThread
	{
		public void startMonitor()
		{
			super.startThread();
		}
		public void stopMonitor()
		{
			try {
				if(null != m_WatchService)
				{
					m_WatchService.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			super.stopThread();
		}
		@Override
		public void run() {
			try {
				m_WatchService = FileSystems.getDefault().newWatchService();
				Paths.get(CLog.s_strConfig).register(m_WatchService,   
		                StandardWatchEventKinds.ENTRY_CREATE,  
		                StandardWatchEventKinds.ENTRY_DELETE,  
		                StandardWatchEventKinds.ENTRY_MODIFY);  
				while(!super.checkQuit())
				{
		            WatchKey key=m_WatchService.take();  
		            for(WatchEvent<?> event:key.pollEvents())  
		            {  
		                //System.out.println(event.context()+"发生了"+event.kind()+"事件");  
		                if(event.context().toString().equals(CLog.s_strLogConfigName))
		                {
		                	CLog.reloadConfig();
		                }
		            }  
		            if(!key.reset())  
		            {  
		                break;  
		            }  
					super.Wait(100);
				}
			} catch (Exception e) {
				if(!e.getClass().getSimpleName().equals("ClosedWatchServiceException"))
				{
					e.printStackTrace();
				}
			} 
		}
		private WatchService m_WatchService;
	}
	
	private static class LogContentFlushThread extends CThread
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!super.checkQuit())
			{
				CLog.flushLogContentCache();
				this.Wait(1000*3);
			}
		}
		
	}
	
	public static void start()
	{
		reloadConfig();
		
		if(null == s_contentCache)
		{
			s_contentCache = new LogContentCache();
		}
		 
		if(null == s_LogWorkQThread)
		{
			s_LogWorkQThread = new CQThread();
			s_LogWorkQThread.startThread();
		}
		if(null == s_ContentFlushThread)
		{
			s_ContentFlushThread = new LogContentFlushThread();
			s_ContentFlushThread.startThread();
		}
		if(null == s_configMonitorThread)
		{
			s_configMonitorThread = new LogConfigMonitorThread();
			s_configMonitorThread.startMonitor();
		}
		s_bStarted = true;
	}
	
	public static void stop()
	{
		if(null != s_configMonitorThread)
		{
			s_configMonitorThread.stopMonitor();
			s_configMonitorThread = null;
		}
		if(null != s_ContentFlushThread)
		{
			s_ContentFlushThread.stopThread();
			s_ContentFlushThread = null;
		}
		if(null != s_LogWorkQThread)
		{
			s_LogWorkQThread.stopThread();
			s_LogWorkQThread = null;
		}
		
		// output cache
		flushLogContentCache();
		if(null != s_contentCache)
		{
			s_contentCache = null;
		}
		s_bStarted = false;
	}

	public static void config_setLogCfg(String dirName, String fileName)
	{
		CFileSystem.createDir(dirName);
		s_strConfig = dirName;
		s_strLogConfigName = fileName;
	}
	public static void config_setLogFile(String dirName, String fileName)
	{
		CFileSystem.createDir(dirName);
		s_strLogDirName = dirName;
		s_strLogName = fileName;
	}
	public static void config_setTag(String tag, boolean enable)
	{
		s_syncObjForTagMap.Lock();
		s_tagMap.put(tag, enable);
		s_syncObjForTagMap.UnLock();
	}
	public static void config_output()
	{
		outputConsole("CLog.config_output ----------------->>>>>> begin\n");
		for (Map.Entry<String, Boolean> entry : s_tagMap.entrySet()) {
			String tag = entry.getKey();
			Boolean enable = entry.getValue();
			outputConsole("tag[%s] enable[%b]\n", tag, enable);
		}
		outputConsole("CLog.config_output ----------------->>>>>> end\n");
	}
	public static void reloadConfig()
	{
		//outputConsole("CLog.reloadConfig \n");
		String xmlStr = "";
		String configFileFullName = s_strConfig + "\\" + s_strLogConfigName;
		File cfile=new File(configFileFullName);
		try
		{
			if(!cfile.exists())
			{
				String defaultContent = ""
						+ "<config>\n"
						+ "\t<tag name='COMMON' output='1' />\n"
						+ "\t<tag name='TEST' output='1' />\n"
						+ "</config>\n";
				CFileSystem.createDir(s_strConfig);
				CFile.fileWrite(configFileFullName, defaultContent, false);
			}
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
				return;
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    StringReader sr = new StringReader(xmlStr);
		    InputSource is = new InputSource(sr);
		    Document doc = builder.parse(is);
		    Element rootElement = doc.getDocumentElement();
		    
		    // 检查返回数据有效性
		    if(!rootElement.getTagName().contains("config")) 
		    	return;
	
		    NodeList tag_contents = rootElement.getElementsByTagName("tag");
	        int lenList = tag_contents.getLength();
	        for (int i = 0; i < lenList; i++) {
	        	Node tag_content = tag_contents.item(i);
	        	String tag_name = ((Element)tag_content).getAttribute("name");
	        	String tag_output = ((Element)tag_content).getAttribute("output");
	        	//outputConsole("name:%s tag_output:%s \n", tag_name, tag_output);
	        	
	        	int output_flg = Integer.parseInt(tag_output);
	        	if(output_flg == 0)
	        	{
	        		config_setTag(tag_name, false);
	        	}
	        	if(output_flg == 1)
	        	{
	        		config_setTag(tag_name, true);
	        	}
	        }
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			return;
		}
	}
	
	public static void error(String target, String format, Object... args)
	{
		String logstr = String.format(format, args);
		output(LOGLEVEL.ERROR, target, logstr);
	}
	public static void error(String format, Object... args)
	{
		String logstr = String.format(format, args);
		output(LOGLEVEL.ERROR, "TEST", logstr);
	}
	
	public static void warning(String target, String format, Object... args)
	{
		String logstr = String.format(format, args);
		output(LOGLEVEL.WARNING, target, logstr);
	}
	public static void warning(String format, Object... args)
	{
		String logstr = String.format(format, args);
		output(LOGLEVEL.WARNING, "TEST", logstr);
	}
	
	public static void info(String target, String format, Object... args)
	{
		String logstr = String.format(format, args);
		output(LOGLEVEL.INFO, target, logstr);
	}
	public static void info(String format, Object... args)
	{
		String logstr = String.format(format, args);
		output(LOGLEVEL.INFO, "TEST", logstr);
	}
	
	public static void debug(String target, String format, Object... args)
	{
		String logstr = String.format(format, args);
		output(LOGLEVEL.DEBUG, target, logstr);
	}
	public static void debug(String format, Object... args)
	{
		String logstr = String.format(format, args);
		output(LOGLEVEL.DEBUG, "TEST", logstr);
	}
	
	private static void output(LOGLEVEL level, String target, String logstr)
	{
		// check is or NOT output log
		s_syncObjForTagMap.Lock();
		if(null != target && "" != target && !s_tagMap.containsKey(target))
		{
			s_tagMap.put(target, false);
		}
		if (LOGLEVEL.ERROR == level || LOGLEVEL.WARNING == level) { // level ERROR WARNING must output
			// do nothing
		} else {
			if (true == s_levelMap.get(level)) { // the level is enabled, then check tag switch
				if(!s_tagMap.containsKey(target) || s_tagMap.get(target) == false)
				{
					s_syncObjForTagMap.UnLock();
					return;
				}
			} else {
				s_syncObjForTagMap.UnLock();
				return;
			}
		}
		s_syncObjForTagMap.UnLock();
		
		// dir file name check
		if(null == s_strLogDirName) s_strLogDirName = "output";
		if(null == s_strLogName) s_strLogName = "default.log";

		String curDateTimeStr = CUtilsDateTime.GetCurDateTimeStr();
		Long TC = CUtilsDateTime.GetCurrentTimeMillis();
		String datetime = String.format("%s.%03d", curDateTimeStr, TC%1000);
		
//		String fullLogStr = "[" + curDateTimeStr + "." + TC + "] " + target + " " + logstr;
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		String fullLogStr = String.format("[%s][%10s] %s", sdf.format(CUtilsDateTime.GetCurDate()), target, logstr);
		
		LogOutRequest cLogOutRequest = new LogOutRequest(datetime, level, target, logstr);
		if(s_bStarted)
		{
			s_LogWorkQThread.postRequest(cLogOutRequest);
		}
		else
		{
			cLogOutRequest.doAction(); // 无log工作线程直接输出
		}
	}
	
	private static void flushLogContentCache()
	{
		if(s_bStarted)
		{
			String currentOutputContent = null;
			
			s_contentCache.lock();
			currentOutputContent = s_contentCache.popContent();
			s_contentCache.unlock();
			
			// 开始输出
			if(null != currentOutputContent && currentOutputContent.length() > 0)
			{
				outputConsole(currentOutputContent);
				outputFile(currentOutputContent);
			}
		}
	}
	
	private static String cvtLogLevel2Str(LOGLEVEL loglevel) 
	{
		switch(loglevel) {
			case ERROR:
				return "E";
			case WARNING:
				return "W";
			case INFO: 
				return "I";
			case DEBUG: 
				return "D";
			default: 
				return "X";
		}
	}
	private static void implLogOutput(String datetime, LOGLEVEL loglevel, String logtag, String logstr)
	{
		String logbuf = null;
		if (logstr.endsWith("\n")) {
			logbuf = String.format("%s %s %s : %s", datetime, cvtLogLevel2Str(loglevel), logtag, logstr);
		} else {
			logbuf = String.format("%s %s %s : %s\n", datetime, cvtLogLevel2Str(loglevel), logtag, logstr);
		}
		
		String currentOutputContent = null;
		
		if(s_bStarted)
		{
			// add to cache and check
			s_contentCache.lock();
			s_contentCache.addContent(logbuf);
			if(s_contentCache.size() > 7*1024)
			{
				currentOutputContent = s_contentCache.popContent();
			}
			s_contentCache.unlock();
		}
		else
		{
			currentOutputContent = logbuf;
		}
		
		// 开始输出
		if(null != currentOutputContent && currentOutputContent.length() > 0)
		{
			outputConsole(currentOutputContent);
			outputFile(currentOutputContent);
		}
	}
	
	private static void outputConsole(String format, Object... args)
	{
		String logstr = String.format(format, args);
		s_fmt.format("%s", logstr);
	}
	private static void outputFile(String format, Object... args)
	{
		String logstr = String.format(format, args);
		File cDir = new File(s_strLogDirName);  
		if (!cDir.exists()  && !cDir.isDirectory())      
		{       
		    cDir.mkdir();    
		}
		File cfile =new File(s_strLogDirName + "\\" + s_strLogName);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile, true);
			cOutputStream.write(logstr.getBytes());
			cOutputStream.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception:" + e.getMessage()); 
		}
	}
	
	static private Formatter s_fmt = new Formatter(System.out);
	static private String s_strLogDirName = "output";
	static private String s_strLogName = "default.log";
	static private String s_strConfig = "config";
	static private String s_strLogConfigName = "log_config.xml";
	static private Map<LOGLEVEL, Boolean> s_levelMap = new HashMap<LOGLEVEL, Boolean>() {
		{
			put(LOGLEVEL.ERROR, true);
			put(LOGLEVEL.WARNING, true);
			put(LOGLEVEL.INFO, true);
			put(LOGLEVEL.DEBUG, true);
		}
	};
	static private Map<String, Boolean> s_tagMap = new HashMap<String, Boolean>() {
		{
			put("TEST", true);
		}
	};
	static private CSyncObj s_syncObjForTagMap = new CSyncObj();

	static private CQThread s_LogWorkQThread = null;
	static private LogConfigMonitorThread s_configMonitorThread = null;
	
	static private LogContentCache s_contentCache = null;
	static private LogContentFlushThread s_ContentFlushThread = null;
	static private boolean s_bStarted = false;
}
