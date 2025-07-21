package pers.di.common;

import java.io.IOException;
import java.nio.file.FileSystems;  
import java.nio.file.Paths;  
import java.nio.file.StandardWatchEventKinds;  
import java.nio.file.WatchEvent;  
import java.nio.file.WatchKey;  
import java.nio.file.WatchService;

import pers.di.common.*;

public class TestCLog {
	
	@CTest.test
	public static void test_CLog()
	{
		CTest.TEST_PERFORMANCE_BEGIN();
		int itestdata = 0;
		long test_cnt = 10000*5;
		for(int i=0; i<test_cnt; i++)
		{
			CLog.debug("TAG1", "testlog TAG1 string abcdedf1 %d!\n", i);
		}
		long cost = CTest.TEST_PERFORMANCE_END();
		CTest.EXPECT_TRUE(cost < 1000);
		CLog.debug("TAG1", "dump[%d] \n", cost);
	}
	public static void main(String[] args) {
		
		CLog.config_setLogCfg("config", "log_config.xml");
		CLog.config_setLogFile("output", "default.log");
		CLog.start();

		CLog.config_setTag("TAG1", true);
		CLog.config_setTag("TAG2", false);
		CLog.config_setTag("TAG3", false);
		
		CTest.ADD_TEST(TestCLog.class);
		CTest.RUN_ALL_TESTS();
	
		CLog.stop();
	}
}
