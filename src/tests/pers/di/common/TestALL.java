package pers.di.common;

import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.common.CThread;

public class TestALL {

	public static void main(String[] args) {
		CSystem.start();
		
		CTest.ADD_TEST(TestCWatiObj.class);
		CTest.ADD_TEST(TestCThread.class);
		CTest.ADD_TEST(TestCQThread.class);
		CTest.ADD_TEST(TestCUtilsDateTime.class);
		CTest.ADD_TEST(TestCDateTimeThruster.class);
	
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
	
}
