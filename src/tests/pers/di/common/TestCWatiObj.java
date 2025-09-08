package pers.di.common;

import pers.di.common.*;

public class TestCWatiObj {
	private static final boolean DEBUG_TESTCASE_LOG = false;
	private static void TESTCASE_LOG(String s) {
		if (DEBUG_TESTCASE_LOG) CLog.debug("TEST", s);
	}
	public static CWaitObject s_cCWaitObject = new CWaitObject();
	public static class TestThread extends Thread
	{
		@Override
		public void run()
		{
			CThread.msleep(500);
			s_cCWaitObject.Notify();
			CThread.msleep(200);
			s_cCWaitObject.Notify();
		}
	}
	@CTest.test
	public void test_CWaitObject()
	{
		TestThread cThread = new TestThread();
		cThread.start();
		
		CThread.msleep(200);
		TESTCASE_LOG("CWaitObject.Wait ...1");
		CTest.EXPECT_TRUE(s_cCWaitObject.Wait(Long.MAX_VALUE));
		TESTCASE_LOG( "CWaitObject.Wait ...1 Return");
		
		TESTCASE_LOG( "CWaitObject.Wait ...2");
		CTest.EXPECT_TRUE(s_cCWaitObject.Wait(Long.MAX_VALUE));
		TESTCASE_LOG( "CWaitObject.Wait ...2 Return");
		
		TESTCASE_LOG("CWaitObject.Wait ...2");
		CTest.EXPECT_FALSE(s_cCWaitObject.Wait(200));
		TESTCASE_LOG("CWaitObject.Wait ...2 Return");
		
	}
	
	public static void main(String[] args) {
		CTest.ADD_TEST(TestCWatiObj.class);
		CTest.RUN_ALL_TESTS();
	}
}
