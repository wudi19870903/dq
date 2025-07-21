package pers.di.common;

import pers.di.common.CLog;
import pers.di.common.CRandom;
import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestCRandom {
	
	@CTest.test
	public static void test_CRandom()
	{
		int ri = 0;
		ri = CRandom.randomInteger();
		ri = CRandom.randomInteger();
		ri = CRandom.randomInteger();
		CLog.debug("TEST", "%d", ri);
	}
	
	public static void main(String[] args) {
		CSystem.start();
		CTest.ADD_TEST(TestCRandom.class);
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
