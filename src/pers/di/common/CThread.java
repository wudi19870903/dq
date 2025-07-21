package pers.di.common;

abstract public class CThread {
	public CThread()
	{
		m_thread = new InThread(this);
	}
	
	abstract public void run();
		
	public boolean checkQuit()
	{
		return m_thread.checkQuit();
	}
	
	public boolean Wait(long msec)
	{
		return m_thread.Wait(msec);
	}
	
	public boolean Notify()
	{
		return m_thread.Notify();
	}
	
	public boolean startThread()
	{
		return m_thread.startThread();
	}
	
	public boolean stopThread()
	{
		return m_thread.stopThread();
	}
	
	public boolean checkRunning()
	{
		return m_thread.checkRunning();
	}
	
	public static void msleep(int msec)
	{
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		};
	}
	
	public static void usleep(int usec)
	{
		try {
			int msec = usec/1000;
			int nsec = (usec%1000)*1000;
			Thread.sleep(msec, nsec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		};
	}
	
	public static void nsleep(int nsec)
	{
		try {
			int msec = nsec/1000000;
			Thread.sleep(msec, nsec%1000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		};
	}
	
	private InThread m_thread;
	
	private class InThread extends Thread
	{
		public InThread(CThread cCThread)
		{
			m_cCThreadRef = cCThread;
			m_bQuit = false;
			m_bRunning = false;
			m_cCWaitObject = new CWaitObject();
		}
		@Override
        public void run()
        {
			m_cCThreadRef.run();
			m_bRunning = false;
        }
		boolean checkQuit()
		{
			return m_bQuit;
		}
		boolean checkRunning()
		{
			return m_bRunning;
		}
		boolean Wait(long msec)
		{
			m_cCWaitObject.Wait(msec);
			return true;
		}
		boolean Notify()
		{
			m_cCWaitObject.Notify();
			return true;
		}
		boolean startThread()
		{
			m_bRunning = true;
			super.start();
			return true;
		}
		boolean stopThread()
		{
			m_bQuit = true;
			m_cCWaitObject.Notify();
			try {
				super.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
		private CThread m_cCThreadRef;
		private boolean m_bQuit;
		private boolean m_bRunning;
		private CWaitObject m_cCWaitObject;
	}
}
