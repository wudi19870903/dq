package pers.di.common;

import pers.di.common.CLog;
import pers.di.common.CTest;
import pers.di.common.CThread;

public class TestCThread {
	
	public static class TestThread extends CThread 
	{
		@Override
		public void run() {
			CLog.debug("TEST", "TestThread Run");
			while(!checkQuit())
			{
				CLog.debug("TEST", "TestThread Running...");
				iRun = 1;
				Wait(Long.MAX_VALUE);
			}
		}
		
	}
	
	public static int iRun = 0;
	
	@CTest.test
	public static void test_CThread()
	{
		iRun = 0;
		CTest.TEST_PERFORMANCE_BEGIN();
		
		TestThread cThread = new TestThread();
		cThread.startThread();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cThread.stopThread();
		
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 150);
		CTest.EXPECT_TRUE(iRun == 1);
	}
	
	public static void main(String[] args) {
		
		CTest.ADD_TEST(TestCThread.class);
		
		CTest.RUN_ALL_TESTS();
	}
}
