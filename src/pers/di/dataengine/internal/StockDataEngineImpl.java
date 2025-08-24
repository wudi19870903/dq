package pers.di.dataengine.internal;

import java.util.Iterator;
import java.util.Map;

import pers.di.common.CDateTimeThruster;
import pers.di.common.CLog;
import pers.di.common.CUtilsDateTime;
import pers.di.dataengine.DAContext;
import pers.di.dataengine.IEngineListener;
import pers.di.dataengine.StockDataEngine;
import pers.di.dataprovider.DataProvider;

public class StockDataEngineImpl extends StockDataEngine {

    public StockDataEngineImpl ()
	{
		m_SharedSession = new SharedSession();
		m_CDateTimeThruster = new CDateTimeThruster();
		CLog.info("DENGINE", "DataRoot: %s", DataProvider.getInstance().dataRoot());
	}

    @Override
    public int config(String key, String value) {
        if(0 == key.compareTo("TriggerMode"))
		{
			if(value.contains("History"))
			{	
				String[] cols = value.split(" ");
				String beginDate = "";
				String endDate = "";
				if (2 == cols.length) {
					beginDate = cols[1];
					endDate = beginDate;
				} else if (3 == cols.length) {
					beginDate = cols[1];
					endDate = cols[2];
				} else {
					CLog.error("DENGINE", "input parameter error! configStr:%s", value);
				}
				
				// 初始化历史交易日表
				if( !CUtilsDateTime.CheckValidDate(beginDate) 
						|| !CUtilsDateTime.CheckValidDate(endDate))
				{
					CLog.error("DENGINE", "input parameter error!");
					m_SharedSession.bConfigFailed = true;
				}
				else
				{
					m_CDateTimeThruster.config("TriggerMode", value);
					m_SharedSession.bHistoryTest = true;
					m_SharedSession.beginDate = beginDate;
					m_SharedSession.endDate = endDate;
					CLog.warning("DENGINE", "config trigger history test: %s -> %s.", beginDate, endDate);
				}
			} else {
				CLog.error("DENGINE", "input parameter error!");
				m_SharedSession.bConfigFailed = true;
			}
		}
		else
		{
			CLog.error("DENGINE", "input parameter error!");
			m_SharedSession.bConfigFailed = true;
		}
		return 0;
    }

    @Override
    public int registerListener(IEngineListener listener) {
        m_SharedSession.listeners.add(listener);
		DAContext cDAContext = new DAContext();
		m_SharedSession.listenerContext.put(listener, cDAContext);
		return 0;
    }

    @Override
    public int unRegisterListener(IEngineListener listener) {
        {
			Iterator<Map.Entry<IEngineListener, DAContext>> iterator = m_SharedSession.listenerContext.entrySet().iterator();
	        while(iterator.hasNext()){
	        	Map.Entry<IEngineListener, DAContext> entry = iterator.next();
	            if (listener == entry.getKey()) {  
	            	iterator.remove();   
	            }  
	        }
		}
		{
			Iterator<IEngineListener> iterator = m_SharedSession.listeners.iterator();  
	        while (iterator.hasNext()) {   
	            if (listener == iterator.next()) {  
	                iterator.remove(); 
	            }  
	        }
		}
		return 0;
    }

    @Override
    public int run() {
        if(m_SharedSession.bConfigFailed) return -1;
		
		// init all task
		m_CDateTimeThruster.schedule(new EngineTaskDayFinish("21:00:00", m_SharedSession));
		
		// callback listener initialize
		for(int i=0; i<m_SharedSession.listeners.size(); i++)
		{
			IEngineListener listener = m_SharedSession.listeners.get(i);
			listener.onInitialize(m_SharedSession.listenerContext.get(listener));
		}
		
		// run CDateTimeThruster
		m_CDateTimeThruster.run();
		
		// callback listener unInitialize
		for(int i=0; i<m_SharedSession.listeners.size(); i++)
		{
			IEngineListener listener = m_SharedSession.listeners.get(i);
			listener.onUnInitialize(m_SharedSession.listenerContext.get(listener));
		}
		
		return 0;
    }

    private SharedSession m_SharedSession;
    private CDateTimeThruster m_CDateTimeThruster;
}
