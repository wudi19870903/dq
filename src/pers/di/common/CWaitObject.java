package pers.di.common;

public class CWaitObject {

	public CWaitObject()
	{
		m_sync = new CSyncObj();
		m_waitObj = new Object();
		m_bNotified = false;
	}

	public boolean Wait(long msec)
	{
		boolean isNotifyReturn = false;
		
		long TCB = CUtilsDateTime.GetCurrentTimeMillis();
		try {
			synchronized(m_waitObj)
			{
				if(!m_bNotified)
				{
					m_waitObj.wait(msec);
					long TCE = CUtilsDateTime.GetCurrentTimeMillis();
					if(TCE - TCB >= msec)
					{
						isNotifyReturn = false;
					}
					else
					{
						isNotifyReturn = true;
					}
				}
				else
				{
					isNotifyReturn = true;
				}
				m_bNotified = false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return isNotifyReturn;
	}
	
	public boolean Notify()
	{
		synchronized(m_waitObj)
		{
			m_waitObj.notify();
			m_bNotified = true;
		}
		return true;
	}
	
	private CSyncObj m_sync;
	private Object m_waitObj;
	private boolean m_bNotified;
}
