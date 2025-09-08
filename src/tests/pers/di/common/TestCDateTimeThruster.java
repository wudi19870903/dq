package pers.di.common;

import java.util.*;

import pers.di.common.*;

public class TestCDateTimeThruster {
	private static final boolean DEBUG_TESTCASE_LOG = false;
	private static void TESTCASE_LOG(String format, Object... args) {
		if (DEBUG_TESTCASE_LOG) CLog.info("TEST", String.format(format, args));
	}

	public static CDateTimeThruster s_CDateTimeThruster = new CDateTimeThruster();
	
	/*
	 * ************************************************************************************
	 */
	private static List<String> testKeyList1 = new ArrayList<String>();
	public static class TestScheduleTask1 extends CDateTimeThruster.ScheduleTask
	{
		public TestScheduleTask1(String name, String time, int pri) {
			super(name, time, pri);
		}

		@Override
		public void doTask(String date, String time) {
			String testKey = String.format("%s %s %05d", date, super.getTime(), super.getPriority());
			//CLog.output("TEST", "TestScheduleTask.doTask testKey=%s", testKey);
			testKeyList1.add(testKey);
			
			CTest.EXPECT_TRUE(s_CDateTimeThruster.GetDateStr().equals(date));
			CTest.EXPECT_TRUE(s_CDateTimeThruster.GetTimeStr().equals(time));
		}
	}
	
	@CTest.test
	public void test_CDateTimeThruster_History()
	{
		testKeyList1.clear();
		
		s_CDateTimeThruster.config("TriggerMode", "HistoryTest 2016-01-01 2017-01-03");
		
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80a", "08:00:00", 5));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80b", "08:00:00", 6));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80c", "08:00:00", 7));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80d", "08:00:00", 8));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80e", "08:00:00", 9));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80f", "08:00:00", 1));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80g", "08:00:00", 0));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80h", "08:00:00", 0));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80i", "08:00:00", 12));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("80j", "08:00:00", 8));
		
		
		s_CDateTimeThruster.schedule(new TestScheduleTask1("82a", "08:02:00", 0));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("82b", "08:02:00", 6));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("82c", "08:02:00", 3));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("82d", "08:02:00", 7));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("82e", "08:02:00", 8));
		
		s_CDateTimeThruster.schedule(new TestScheduleTask1("07a", "07:00:00", 0));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("07b", "07:00:00", 6));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("07c", "07:00:00", 1));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("07d", "07:00:00", 7));
		s_CDateTimeThruster.schedule(new TestScheduleTask1("07e", "07:00:00", 8));
		
		
		s_CDateTimeThruster.schedule(new TestScheduleTask1("01", "01:00:00", 8));
		
		s_CDateTimeThruster.schedule(new TestScheduleTask1("04", "04:00:00", 8));
		
		s_CDateTimeThruster.schedule(new TestScheduleTask1("12", "12:00:00", 8));
		
		s_CDateTimeThruster.schedule(new TestScheduleTask1("11", "11:22:00", 8));
		
		s_CDateTimeThruster.run();
		
		CTest.EXPECT_LONG_EQ(testKeyList1.size(), 8856);
		TESTCASE_LOG("TESTCASE_LOG(sk count %d", testKeyList1.size());
		
		for(int i=0; i<testKeyList1.size()-1;i++)
		{
			String testKey1 = testKeyList1.get(i);
			String testKey2 = testKeyList1.get(i+1);
			//CLog.output("TEST", "%s", testKey1);
			if(testKey1.compareTo(testKey2) <= 0)
			{
				CTest.EXPECT_TRUE(true);
			}
			else
			{
				CTest.EXPECT_TRUE(false);
			}
		}
	}
	
	/*
	 * ************************************************************************************
	 */
	private static List<String> testKeyList2 = new ArrayList<String>();
	public static class TestScheduleTask2 extends CDateTimeThruster.ScheduleTask
	{
		public TestScheduleTask2(String name, String time, int pri) {
			super(name, time, pri);
		}

		@Override
		public void doTask(String date, String time) {
			String testKey = String.format("%s %s %05d", date, super.getTime(), super.getPriority());
			//CLog.output("TEST", "TestScheduleTask.doTask testKey=%s", testKey);
			testKeyList2.add(testKey);
			
			CTest.EXPECT_TRUE(s_CDateTimeThruster.GetDateStr().equals(date));
			CTest.EXPECT_TRUE(s_CDateTimeThruster.GetTimeStr().equals(time));
		}
	}
	
	public static class TestInsertThread extends CThread
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			this.msleep(3000);
			String curTime = CUtilsDateTime.GetCurTimeStr();
			String curTime_add2s = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTime, 2);
			s_CDateTimeThruster.schedule(new TestScheduleTask2("TT", curTime_add2s, 0));
		}
		
	}
	
	//@CTest.test
	public void test_CDateTimeThruster_Realtime()
	{
		s_CDateTimeThruster = new CDateTimeThruster();
		testKeyList2.clear();
		
		s_CDateTimeThruster.config("TriggerMode", "Realtime");
		
		String curDate = CUtilsDateTime.GetCurDateStr();
		String curTime = CUtilsDateTime.GetCurTimeStr();
		String curTime_add5s = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTime, 10);
		String curTime_add10s = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTime, 10);
		String curTime_add20s = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTime, 20);
		s_CDateTimeThruster.schedule(new TestScheduleTask2("x", curTime_add10s, 5));
//		s_CDateTimeThruster.schedule(new TestScheduleTask("y", curTime_add10s, 1));
//		s_CDateTimeThruster.schedule(new TestScheduleTask("z", curTime_add20s, 6));
		
		TestInsertThread cTestThread = new TestInsertThread();
		cTestThread.startThread();
		s_CDateTimeThruster.run();
	}
	
	
	/*
	 * ************************************************************************************
	 */
	private static List<String> testKeyList3 = new ArrayList<String>();
	public static class TestScheduleTask3 extends CDateTimeThruster.ScheduleTask
	{
		public TestScheduleTask3(String name, String time, int pri) {
			super(name, time, pri);
		}

		@Override
		public void doTask(String date, String time) {
			String testKey = String.format("%s %s %05d", date, super.getTime(), super.getPriority());
			//CLog.output("TEST", "TestScheduleTask.doTask testKey=%s", testKey);
			testKeyList3.add(testKey);
			
			CTest.EXPECT_TRUE(s_CDateTimeThruster.GetDateStr().equals(date));
			CTest.EXPECT_TRUE(s_CDateTimeThruster.GetTimeStr().equals(time));
		}
	}
	@CTest.test
	public void test_CDateTimeThruster_performacecheck()
	{
		s_CDateTimeThruster.config("TriggerMode", "HistoryTest 2016-01-01 2017-01-03");
		
		for(String time="09:30:00"; time.compareTo("11:30:00")<=0; 
				time=CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(time, 60))
		{
			s_CDateTimeThruster.schedule(new TestScheduleTask3("", time, 0));
		}
		for(String time="13:00:00"; time.compareTo("15:00:00")<=0; 
				time=CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(time, 60))
		{
			s_CDateTimeThruster.schedule(new TestScheduleTask3("", time, 0));
		}
		
		s_CDateTimeThruster.run();
		
		if(testKeyList1.size() != 0)
		{
			CTest.EXPECT_LONG_EQ(testKeyList1.size(), 8856);
		}
		CTest.EXPECT_LONG_EQ(testKeyList3.size(), 89298);
		
	}
	public static void main(String[] args) {
		CSystem.start();
		//CLog.config_setTag("DataEngine", true);
		CTest.ADD_TEST(TestCDateTimeThruster.class);
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
