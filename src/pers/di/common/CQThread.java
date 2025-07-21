package pers.di.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CQThread {
	
	abstract public static class CQThreadRequest
	{
		abstract public void doAction();
	}
	
	public CQThread()
	{
		m_thread = new CQThreadEntity();
	}
	public boolean postRequest(CQThreadRequest cReq)
	{
		if(m_thread.checkRunning())
		{
			return m_thread.postRequest(cReq);
		}
		return false;
	}
	
	public boolean startThread()
	{
		return m_thread.startThread();
	}
	
	public boolean stopThread()
	{
		return m_thread.stopThread();
	}
	
	
	private static class CQThreadEntity extends CThread
	{
		public CQThreadEntity()
		{
//			m_requestList = new LinkedList<CQThreadRequest>();
//			m_syncObj = new CSyncObj();
			
			m_requestConcurrentLinkedQueue = new ConcurrentLinkedQueue<CQThreadRequest>();
		}
		@Override
		public void run() {
			while(!super.checkQuit())
			{
				while(true)
				{
					CQThreadRequest cReq = popRequest();
					if(null == cReq) break;
					cReq.doAction();
				}
				super.Wait(Long.MAX_VALUE);
			}
			while(true)
			{
				CQThreadRequest cReq = popRequest();
				if(null == cReq) break;
				cReq.doAction();
			}
		}
		public boolean postRequest(CQThreadRequest cReq)
		{
//			m_syncObj.Lock();
//			m_requestList.add(cReq);
//			m_syncObj.UnLock();
			
			m_requestConcurrentLinkedQueue.add(cReq);
			
			super.Notify();
			return true;
		}
		public CQThreadRequest popRequest()
		{
			CQThreadRequest cReq = null;
			
//			m_syncObj.Lock();
//			if(m_requestList.size()>0)
//			{
//				cReq = m_requestList.get(0);
//				m_requestList.remove(0);
//			}
//			m_syncObj.UnLock();
			
			cReq = m_requestConcurrentLinkedQueue.poll();
			
			return cReq;
		}

//		private List<CQThreadRequest> m_requestList;
//		private CSyncObj m_syncObj;
		
		private ConcurrentLinkedQueue<CQThreadRequest> m_requestConcurrentLinkedQueue;
	}
	
	private CQThreadEntity m_thread;
}
