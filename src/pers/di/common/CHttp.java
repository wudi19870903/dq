package pers.di.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class CHttp {
	public String getWebData(String urlStr, int mslimitAccessSpeed) {
		
		limitAccessSpeed(mslimitAccessSpeed);
		
		String retData = null;

		try {
			
			URL url;
			url = new URL(urlStr);
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();    

	        conn.setConnectTimeout(5*1000);  //设置连接超时间 
	        conn.setReadTimeout(15*1000); //设置读取超时时间
	        
	        //防止屏蔽程序抓取而返回403错误  
	        conn.setRequestProperty("User-Agent", getRandomUserAgent());  
	        //得到输入流  
	        InputStream inputStream = conn.getInputStream();   
	        //获取自己数组  
	        byte[] getData = readInputStream(inputStream);    
	        String data = new String(getData, "gbk");
	        
	        retData = data;
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Exception[CHttp]:" + e.getMessage()); 
			System.out.println("Exception[CHttp] url:" + urlStr); 
		}    
		
        //System.out.println(data.toString()); 
//        //文件保存位置  
//        File file = new File("D:/test.txt");      
//        FileOutputStream fos = new FileOutputStream(file);       
//        fos.write(getData);   
//        if(fos!=null){  
//            fos.close();    
//        }  
//        if(inputStream!=null){  
//            inputStream.close();  
//        }  
        
        return retData;
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
    
	private void limitAccessSpeed(int msec)
	{
		long curTC = CUtilsDateTime.GetCurrentTimeMillis();
		if(curTC-m_lastAccessTC>msec)
		{
			m_lastAccessTC = curTC;
			return;
		}
		else
		{
			long waitms = msec - (curTC-m_lastAccessTC);
			try {
				Thread.sleep(waitms);
				m_lastAccessTC = CUtilsDateTime.GetCurrentTimeMillis();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String getRandomUserAgent()
	{
		int iSize = m_userAgentList.size();
		int iCurRandom = CRandom.randomUnsignedInteger()%iSize;
		String curUserAgent = m_userAgentList.get(iCurRandom);
		return curUserAgent;
	}
	
    /*
	 * **************************************************************************************
	 */
	private long m_lastAccessTC = 0;
	private List<String> m_userAgentList = Arrays.asList(
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1", 
	        "Mozilla/5.0 (X11; CrOS i686 2268.111.0) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11", 
	        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6", 
	        "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1090.0 Safari/536.6", 
	        "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/19.77.34.5 Safari/537.1", 
	        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.9 Safari/536.5", 
	        "Mozilla/5.0 (Windows NT 6.0) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.36 Safari/536.5", 
	        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3", 
	        "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3", 
	        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_0) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3", 
	        "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1062.0 Safari/536.3", 
	        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1062.0 Safari/536.3", 
	        "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3", 
	        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3", 
	        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3", 
	        "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.0 Safari/536.3", 
	        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.24 (KHTML, like Gecko) Chrome/19.0.1055.1 Safari/535.24", 
	        "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/535.24 (KHTML, like Gecko) Chrome/19.0.1055.1 Safari/535.24",
	        "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)"
	        );
}
