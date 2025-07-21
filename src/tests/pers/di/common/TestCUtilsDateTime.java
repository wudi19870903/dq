package pers.di.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import pers.di.common.*;
import pers.di.common.CTest.test;

public class TestCUtilsDateTime {

	@test
	public void test_GetCurrentTimeMillis() {
		CTest.TEST_PERFORMANCE_BEGIN();
		long testdata = 0;
		long test_cnt = 10000 * 100;
		for(int i=0; i<test_cnt; i++)
		{
			testdata = testdata + CUtilsDateTime.GetCurrentTimeMillis();
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 50);
		CLog.debug("TEST", "dump:%d", testdata);
	}
	
	@test
	public void test_CheckValidDate()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		boolean testdata = false;
		long test_cnt = 10000 * 100;
		for(int i=0; i<test_cnt; i++)
		{
			testdata = testdata & CUtilsDateTime.CheckValidDate("2017-02-04");
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 100);
		CLog.debug("TEST", "dump:%b", testdata);
		
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2016--02-04"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2016--2-29"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2016-0--28"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("201a-01-28"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2010-0a-28"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2010-01-2a"));
		
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2016-00-28"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2016-13-28"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2016-01-00"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2016-01-32"));
		
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDate("2016-02-04"));
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDate("2016-02-29"));
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDate("2016-02-28"));
		
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDate("2017-02-04"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2017-02-29"));
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDate("2017-02-28"));
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDate("2017-03-31"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2017-04-31"));
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDate("2017-12-31"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2017-12-32"));
		
		CObjectContainer<Integer> year = new CObjectContainer<Integer>();
		CObjectContainer<Integer> month = new CObjectContainer<Integer>();
		CObjectContainer<Integer> day = new CObjectContainer<Integer>();
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDate("2017-02-29", year, month, day));
		CTest.EXPECT_TRUE(null == year.get());
		CTest.EXPECT_TRUE(null == month.get());
		CTest.EXPECT_TRUE(null == day.get());
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDate("2017-02-28", year, month, day));
		CTest.EXPECT_LONG_EQ(year.get(), 2017);
		CTest.EXPECT_LONG_EQ(month.get(), 2);
		CTest.EXPECT_LONG_EQ(day.get(), 28);
	}
	
	@test
	public void test_CheckValidTime()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		boolean testdata = false;
		long test_cnt = 10000 * 100;
		for(int i=0; i<test_cnt; i++)
		{
			testdata = testdata & CUtilsDateTime.CheckValidTime("12:31:22");
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 100);
		CLog.debug("TEST", "dump:%b", testdata);
		
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("21::31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("21::1:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("21:31::2"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("2a:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("21:3a:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("21:31:2a"));
		
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidTime("23:31:01"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("24:31:01"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("23:60:01"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("23:31:60"));
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidTime("00:00:00"));
		
		CObjectContainer<Integer> hour = new CObjectContainer<Integer>();
		CObjectContainer<Integer> minute = new CObjectContainer<Integer>();
		CObjectContainer<Integer> second = new CObjectContainer<Integer>();
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidTime("24:31:01", hour, minute, second));
		CTest.EXPECT_TRUE(null == hour.get());
		CTest.EXPECT_TRUE(null == minute.get());
		CTest.EXPECT_TRUE(null == second.get());
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidTime("12:33:21", hour, minute, second));
		CTest.EXPECT_LONG_EQ(hour.get(), 12);
		CTest.EXPECT_LONG_EQ(minute.get(), 33);
		CTest.EXPECT_LONG_EQ(second.get(), 21);
	}
	
	@test
	public void test_CheckValidDateTime()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		boolean testdata = false;
		long test_cnt = 10000 * 100;
		for(int i=0; i<test_cnt; i++)
		{
			testdata = testdata & CUtilsDateTime.CheckValidDateTime("2017-02-21 12:31:22");
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 250);
		CLog.debug("TEST", "dump:%b", testdata);
		
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-2112:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21x12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:31:22?"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("201--02-21 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-0--21 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-2- 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21 -2:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:-1:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:31:-2"));
	
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-13-21 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-00-21 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-00 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-32 12:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21 24:31:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:60:22"));
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:31:60"));
		
		CObjectContainer<Integer> year = new CObjectContainer<Integer>();
		CObjectContainer<Integer> month = new CObjectContainer<Integer>();
		CObjectContainer<Integer> day = new CObjectContainer<Integer>();
		CObjectContainer<Integer> hour = new CObjectContainer<Integer>();
		CObjectContainer<Integer> minute = new CObjectContainer<Integer>();
		CObjectContainer<Integer> second = new CObjectContainer<Integer>();
		CTest.EXPECT_FALSE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:31:60", 
				year, month, day, hour, minute, second));
		CTest.EXPECT_TRUE(null == year.get());
		CTest.EXPECT_TRUE(null == month.get());
		CTest.EXPECT_TRUE(null == day.get());
		CTest.EXPECT_TRUE(null == hour.get());
		CTest.EXPECT_TRUE(null == minute.get());
		CTest.EXPECT_TRUE(null == second.get());
		CTest.EXPECT_TRUE(CUtilsDateTime.CheckValidDateTime("2017-02-21 12:31:59", 
				year, month, day, hour, minute, second));
		CTest.EXPECT_LONG_EQ(year.get(), 2017);
		CTest.EXPECT_LONG_EQ(month.get(), 2);
		CTest.EXPECT_LONG_EQ(day.get(), 21);
		CTest.EXPECT_LONG_EQ(hour.get(), 12);
		CTest.EXPECT_LONG_EQ(minute.get(), 31);
		CTest.EXPECT_LONG_EQ(second.get(), 59);
	}
	
	
	@test
	public void test_GetCurDate() {
		CTest.TEST_PERFORMANCE_BEGIN();
		long test_cnt = 10000 * 100;
		Date cDate = null;
		for(int i=0; i<test_cnt; i++)
		{
			cDate = CUtilsDateTime.GetCurDate();
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 50);
		CLog.debug("TEST", "dump:%s" , cDate.toString());
	}
	
	@test
	public static void test_GetCurDateTimeStr()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		String stestdata = "";
		long test_cnt = 10000*100;
		for(int i=0; i<test_cnt; i++)
		{
			stestdata = CUtilsDateTime.GetCurDateTimeStr();
			//CThread.sleep(1000);
			//CLog.output("TEST", "dump:%s \n", stestdata);
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 50);
		CLog.debug("TEST", "dump:%s", stestdata);
	}
	
	@test
	public static void test_GetCurDateStr()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		String stestdata = "";
		long test_cnt = 10000*100;
		for(int i=0; i<test_cnt; i++)
		{
			stestdata = CUtilsDateTime.GetCurDateStr();
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 100);
		CLog.debug("TEST", "dump[%s]", stestdata);
	}
	
	@test
	public static void test_GetCurTimeStr()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		String stestdata = "";
		long test_cnt = 10000*100;
		for(int i=0; i<test_cnt; i++)
		{
			stestdata = CUtilsDateTime.GetCurTimeStr();
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 50);
		CLog.debug("TEST", "dump[%s]", stestdata);
	}
	
	@test
	public static void test_getDateStrForSpecifiedDateOffsetD()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		String stestdata = "";
		long test_cnt = 10000*100;
		for(int i=0; i<test_cnt; i++)
		{
			stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("2016-01-31", 2);
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 2000);
		CTest.EXPECT_STR_EQ(stestdata, stestdata);
		
		// +
		stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("2016-01-15", 6);
		CTest.EXPECT_STR_EQ("2016-01-21", stestdata);
		
		stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("2016-08-15", 6);
		CTest.EXPECT_STR_EQ("2016-08-21", stestdata);
		
		stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("2016-01-25", 6);
		CTest.EXPECT_STR_EQ("2016-01-31", stestdata);
		
		stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("1999-02-28", 1);
		CTest.EXPECT_STR_EQ("1999-03-01", stestdata);
		
		String baseDate = "1999-01-01";
		for(int iTest=0; iTest<1000; iTest++)
		{
			String testdateExpect = getDateStrForSpecifiedDateOffsetD_OK(baseDate, iTest);
			String testdate = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD(baseDate, iTest);
			
			CTest.EXPECT_STR_EQ(testdateExpect, testdate);
		}
		
		// -
		stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("2016-01-15", -6);
		CTest.EXPECT_STR_EQ("2016-01-09", stestdata);
		
		stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("2016-08-15", -6);
		CTest.EXPECT_STR_EQ("2016-08-09", stestdata);
		
		stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("2016-01-05", -6);
		CTest.EXPECT_STR_EQ("2015-12-30", stestdata);
		
		stestdata = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD("1999-03-01", -1);
		CTest.EXPECT_STR_EQ("1999-02-28", stestdata);
		
		baseDate = "2030-01-01";
		for(int iTest=0; iTest<5000; iTest++)
		{
			String testdateExpect = getDateStrForSpecifiedDateOffsetD_OK(baseDate, -iTest);
			String testdate = CUtilsDateTime.getDateStrForSpecifiedDateOffsetD(baseDate, -iTest);
			
			CTest.EXPECT_STR_EQ(testdateExpect, testdate);
		}
		
	}
	
	private static String getDateStrForSpecifiedDateOffsetD_OK(String dateStr, int add)
	{
	      Calendar c = Calendar.getInstance();  
	      Date date = null;  
	      try {  
	          date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);  
	      } catch (Exception e) {  
	          e.printStackTrace();  
	      }  
	      c.setTime(date);  
	      int day = c.get(Calendar.DATE);  
	      c.set(Calendar.DATE, day + add);  
	
	      String dayNew = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());  
	      return dayNew;  
	}
	
	@test
	public static void test_getTimeStrForSpecifiedTimeOffsetS()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		String stestdata = "";
		long test_cnt = 10000*100;
		for(int i=0; i<test_cnt; i++)
		{
			stestdata = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS("12:33:05", 3600*2+60*40+60);
			//String afterTimeStr = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetM("12:22:34", 2);
		}
		CTest.EXPECT_TRUE(stestdata.equals("15:14:05"));
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 200);
		CLog.debug("TEST", "dump[%s]", stestdata);
	}
	
	@test
	public static void test_GetSecondFromTimeStr()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		int itestdata = 0;
		long test_cnt = 10000*100;
		for(int i=0; i<test_cnt; i++)
		{
			itestdata = CUtilsDateTime.GetSecondFromTimeStr("12:34:56");
		}
		CTest.EXPECT_LONG_EQ(CUtilsDateTime.GetSecondFromTimeStr("12:34:56"),12*3600+34*60+56);
		CTest.EXPECT_TRUE(CUtilsDateTime.GetSecondFromTimeStr("25:34:56") == 0);
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 50);
		CLog.debug("TEST", "dump[%d]", itestdata);
	}
	
	@test
	public static void test_GetTimeStrFromSecond()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		String stestdata = "'";
		long test_cnt = 10000*100;
		for(int i=0; i<test_cnt; i++)
		{
			stestdata = CUtilsDateTime.GetTimeStrFromSecond(25*3600+61*60+72);
		}
		CTest.EXPECT_TRUE(stestdata.equals("02:02:12"));
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 200);
		CLog.debug("TEST", "dump[%s]", stestdata);
	}
	
	@test
	public static void test_waitDateTime()
	{
		String curDateStr = CUtilsDateTime.GetCurDateStr();
		String curTimeStr = CUtilsDateTime.GetCurTimeStr();
		String beforeTimeStr = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTimeStr, -1);
		String afterTimeStr = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTimeStr, 1);
		CLog.debug("TEST", "curDateStr %s beforeTimeStr = %s", curDateStr, beforeTimeStr);
		CLog.debug("TEST", "curDateStr %s afterTimeStr = %s", curDateStr, afterTimeStr);
		CUtilsDateTime.WAITRESULT wr_before = CUtilsDateTime.waitFor(curDateStr, beforeTimeStr);
		CTest.EXPECT_TRUE(wr_before==CUtilsDateTime.WAITRESULT.TIME_HAS_GONE);
		CUtilsDateTime.WAITRESULT wr_after = CUtilsDateTime.waitFor(curDateStr, afterTimeStr);
		CTest.EXPECT_TRUE(wr_after==CUtilsDateTime.WAITRESULT.TIME_IS_UP);
	}
	
	public static class TestThread extends Thread
	{
		@Override
		public void run()
		{
			CThread.msleep(2000);
			s_waitObj.Notify();
		}
	}
	private static CWaitObject s_waitObj = new CWaitObject();
	@test
	public static void test_waitDateTimeWithObject()
	{
		String curDateStr = CUtilsDateTime.GetCurDateStr();
		String curTimeStr = CUtilsDateTime.GetCurTimeStr();
		String beforeTimeStr = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTimeStr, -2);
		String afterTimeStr = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTimeStr, 1);
		String afterTimeStrlong = CUtilsDateTime.getTimeStrForSpecifiedTimeOffsetS(curTimeStr, 100);
		CLog.debug("TEST", "curDateStr %s beforeTimeStr = %s", curDateStr, beforeTimeStr);
		CLog.debug("TEST", "curDateStr %s afterTimeStr = %s", curDateStr, afterTimeStr);
		CLog.debug("TEST", "curDateStr %s afterTimeStr = %s", curDateStr, afterTimeStrlong);
		CUtilsDateTime.WAITRESULT wr_before = CUtilsDateTime.waitFor(curDateStr, beforeTimeStr, s_waitObj);
		CTest.EXPECT_TRUE(wr_before==CUtilsDateTime.WAITRESULT.TIME_HAS_GONE);
		CUtilsDateTime.WAITRESULT wr_after = CUtilsDateTime.waitFor(curDateStr, afterTimeStr, s_waitObj);
		CTest.EXPECT_TRUE(wr_after==CUtilsDateTime.WAITRESULT.TIME_IS_UP);
		TestThread cTestThread = new TestThread();
		cTestThread.start();
		CUtilsDateTime.WAITRESULT wr_afterlong = CUtilsDateTime.waitFor(curDateStr, afterTimeStrlong, s_waitObj);
		CTest.EXPECT_TRUE(wr_afterlong==CUtilsDateTime.WAITRESULT.OBJECT_IS_NOTIFIED);
	}

	@CTest.test
	public static void test_subTime()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		String stestdata = "'";
		long test_cnt = 10000*100;
		String time1 = "15:22:05";
		String time2 = "13:20:21";
		long diffSec = 0;
		for(int i=0; i<test_cnt; i++)
		{
			diffSec = CUtilsDateTime.subTime(time1,time2);
			
		}
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 250);
		CTest.EXPECT_LONG_EQ(diffSec, subTime_OK(time1, time2));
	}
	private static long subTime_OK(String time1, String time2)
	{
    	long diffsec = 0;
		try {
			Date date1 = new SimpleDateFormat("HH:mm:ss").parse(time1);
			Date date2 = new SimpleDateFormat("HH:mm:ss").parse(time2);  
	        long diff = date1.getTime() - date2.getTime();
	        diffsec = diff / 1000; 
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		} 
		return diffsec;
	}
	
	public static void main(String[] args) {
		
		CSystem.start();
		
		CTest.ADD_TEST(TestCUtilsDateTime.class);
		CTest.RUN_ALL_TESTS();
	
		CSystem.stop();
	}
}
