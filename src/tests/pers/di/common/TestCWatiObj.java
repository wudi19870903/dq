package pers.di.common;

import pers.di.common.*;

public class TestCWatiObj {
	
	public static CWaitObject s_cCWaitObject = new CWaitObject();
	public static class TestThread extends Thread
	{
		@Override
		public void run()
		{
			CLog.debug("TEST", "TestThread run begin");
			CThread.msleep(5000);
			s_cCWaitObject.Notify();
			CThread.msleep(200);
			s_cCWaitObject.Notify();
			CLog.debug("TEST", "TestThread run end");
		}
	}
	@CTest.test
	public void test_CWaitObject()
	{
		TestThread cThread = new TestThread();
		cThread.start();
		
		CThread.msleep(200);
		CLog.debug("TEST", "CWaitObject.Wait ...1");
		CTest.EXPECT_TRUE(s_cCWaitObject.Wait(Long.MAX_VALUE));
		CLog.debug("TEST", "CWaitObject.Wait ...1 Return");
		
		CLog.debug("TEST", "CWaitObject.Wait ...2");
		CTest.EXPECT_TRUE(s_cCWaitObject.Wait(Long.MAX_VALUE));
		CLog.debug("TEST", "CWaitObject.Wait ...2 Return");
		
		CLog.debug("TEST", "CWaitObject.Wait ...2");
		CTest.EXPECT_FALSE(s_cCWaitObject.Wait(200));
		CLog.debug("TEST", "CWaitObject.Wait ...2 Return");
		
	}
	
	public static void main(String[] args) {
		CTest.ADD_TEST(TestCWatiObj.class);
		CTest.RUN_ALL_TESTS();
	}
}
