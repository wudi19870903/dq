package pers.di.common;

import java.util.List;

import pers.di.common.CL2Property;
import pers.di.common.CLog;
import pers.di.common.CRandom;
import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestCL2Property {
	
	@CTest.test
	public static void test_CL2Property_w_r()
	{
		CL2Property cCL2Property = new CL2Property(CSystem.getRunSessionRoot() + "/1.xml");
		
		cCL2Property.setProperty("000001", "checkprice", "3.12");
		cCL2Property.setProperty("000002", "checkprice", "22");
		cCL2Property.setProperty("000002", "checkvolume", "998");
		cCL2Property.setProperty("000003", "pl2i", "value3");
		CTest.EXPECT_STR_EQ(cCL2Property.getProperty("000001", "checkprice"), "3.12");
		
		cCL2Property.setProperty("000001", "checkprice", "3.13");
		CTest.EXPECT_STR_EQ(cCL2Property.getProperty("000001", "checkprice"), "3.13");
		
		cCL2Property.setPropertyExBoolean("000001", "BB1", true);
		CTest.EXPECT_TRUE(cCL2Property.getPropertyExBoolean("000001", "BB1"));
		
		cCL2Property.setPropertyExLong("000001", "LL1", 12345);
		CTest.EXPECT_LONG_EQ(cCL2Property.getPropertyExLong("000001", "LL1"), 12345);
		
		cCL2Property.setPropertyExDouble("000001", "DD1", 8.982);
		CTest.EXPECT_DOUBLE_EQ(cCL2Property.getPropertyExDouble("000001", "DD1"), 8.982);
		
		cCL2Property.sync2file();
	}
	
	@CTest.test
	public static void test_CL2Property_sync2file_sync2mem()
	{
		CL2Property cCL2Property = new CL2Property(CSystem.getRunSessionRoot() + "/1.xml");
		
		cCL2Property.setProperty("000001", "checkprice", "3.12");
		cCL2Property.setProperty("000002", "checkprice", "22");
		cCL2Property.setProperty("000002", "checkvolume", "998");
		cCL2Property.setProperty("000003", "pl2i", "value3");
		CTest.EXPECT_STR_EQ(cCL2Property.getProperty("000001", "checkprice"), "3.12");
		CTest.EXPECT_STR_EQ(cCL2Property.getProperty("000002", "checkvolume"), "998");
		CTest.EXPECT_STR_EQ(cCL2Property.getProperty("000003", "pl2i"), "value3");
		
		cCL2Property.setProperty("000001", "checkprice", "3.13");
		CTest.EXPECT_STR_EQ(cCL2Property.getProperty("000001", "checkprice"), "3.13");
		CTest.EXPECT_STR_EQ(cCL2Property.getProperty("000002", "checkvolume"), "998");
		CTest.EXPECT_STR_EQ(cCL2Property.getProperty("000003", "pl2i"), "value3");
		
		cCL2Property.sync2file();
		
		
		CL2Property cCL2PropertyRead = new CL2Property(CSystem.getRunSessionRoot() + "/1.xml");
		cCL2PropertyRead.sync2mem();
		CTest.EXPECT_STR_EQ(cCL2PropertyRead.getProperty("000001", "checkprice"), "3.13");
		CTest.EXPECT_STR_EQ(cCL2PropertyRead.getProperty("000002", "checkvolume"), "998");
		CTest.EXPECT_STR_EQ(cCL2PropertyRead.getProperty("000003", "pl2i"), "value3");
	}
	
	@CTest.test
	public static void test_CL2Property_size()
	{
		CL2Property cCL2Property = new CL2Property(CSystem.getRunSessionRoot() + "/1.xml");
		cCL2Property.setProperty("000001", "checkprice", "3.12");
		cCL2Property.setProperty("000002", "checkprice", "22");
		cCL2Property.setProperty("000002", "checkvolume", "998");
		cCL2Property.setProperty("000003", "pl2i", "value3");
		
		CTest.EXPECT_LONG_EQ(cCL2Property.size(), 3);
		CTest.EXPECT_LONG_EQ(cCL2Property.size("000002"), 2);
		CTest.EXPECT_LONG_EQ(cCL2Property.size("000001"), 1);

		cCL2Property.sync2file();
	}
	
	@CTest.test
	public static void test_CL2Property_clear()
	{
		CL2Property cCL2Property = new CL2Property(CSystem.getRunSessionRoot() + "/1.xml");
		cCL2Property.setProperty("000001", "checkprice", "3.12");
		cCL2Property.setProperty("000002", "checkprice", "22");
		cCL2Property.setProperty("000002", "checkvolume", "998");
		cCL2Property.setProperty("000003", "pl2i", "value3");
		
		CTest.EXPECT_LONG_EQ(cCL2Property.size(), 3);
		CTest.EXPECT_LONG_EQ(cCL2Property.size("000002"), 2);
		CTest.EXPECT_LONG_EQ(cCL2Property.size("000001"), 1);
		
		cCL2Property.clear("000002");
		CTest.EXPECT_LONG_EQ(cCL2Property.size(), 2);
		CTest.EXPECT_LONG_EQ(cCL2Property.size("000002"), 0);
		
		cCL2Property.clear();
		CTest.EXPECT_LONG_EQ(cCL2Property.size(), 0);

		cCL2Property.sync2file();
	}
	
	@CTest.test
	public static void test_CL2Property_contains()
	{
		CL2Property cCL2Property = new CL2Property(CSystem.getRunSessionRoot() + "/1.xml");
		cCL2Property.setProperty("000001", "checkprice", "3.12");
		cCL2Property.setProperty("000002", "checkprice", "22");
		cCL2Property.setProperty("000002", "checkvolume", "998");
		cCL2Property.setProperty("000003", "pl2i", "value3");
		
		CTest.EXPECT_TRUE(cCL2Property.contains("000002"));
		CTest.EXPECT_TRUE(cCL2Property.contains("000002", "checkprice"));
		CTest.EXPECT_FALSE(cCL2Property.contains("000002", "checkprice1"));
		CTest.EXPECT_FALSE(cCL2Property.contains("000004"));
	}
	
	@CTest.test
	public static void test_CL2Property_list()
	{
		CL2Property cCL2Property = new CL2Property(CSystem.getRunSessionRoot() + "/1.xml");
		cCL2Property.setProperty("000001", "checkprice", "3.12");
		cCL2Property.setProperty("000002", "checkprice", "22");
		cCL2Property.setProperty("000002", "checkvolume", "998");
		cCL2Property.setProperty("000003", "pl2i", "value3");
		
		List<String> listmain = cCL2Property.list();
		CTest.EXPECT_LONG_EQ(listmain.size(), 3);
		
		List<String> listsub = cCL2Property.list("000002");
		CTest.EXPECT_LONG_EQ(listsub.size(), 2);
	}
	
	public static void main(String[] args) {
		CSystem.start();
		CTest.ADD_TEST(TestCL2Property.class);
		CTest.RUN_ALL_TESTS("TestCL2Property.");
		CSystem.stop();
	}
}
