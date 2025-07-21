package pers.di.common;

import pers.di.common.CFileSystem;
import pers.di.common.CSystem;
import pers.di.common.CTest;


public class TestCSystem {
	
	
	@CTest.test
	public static void test_CSystem()
	{
		String rwRoot = CSystem.getRWRoot();
		CTest.EXPECT_TRUE(rwRoot.endsWith("rw"));
		
		
		String runSesstionRoot = CSystem.getRunSessionRoot();
		String runSesstionRootStablePart = CFileSystem.getParentDir(runSesstionRoot);
		CTest.EXPECT_TRUE(runSesstionRootStablePart.endsWith("RunSession"));
	}
	
	public static void main(String[] args) {
		
		CTest.ADD_TEST(TestCSystem.class);
		
		CTest.RUN_ALL_TESTS();
	}
}
