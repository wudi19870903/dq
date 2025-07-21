package pers.di.common;

import pers.di.common.*;
import pers.di.common.CQThread.*;

public class TestCQThread {
	
	public static class TestRequest extends CQThreadRequest
	{
		public TestRequest(int index)
		{
			m_index = index;
		}
		@Override
		public void doAction() {
			try {
				s_iCalled++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public int m_index;
	}
	
	
	public static int s_iCalled = 0;
	@CTest.test
	public void test_CQThreadRequest()
	{
		int iPostCnt = 10000*100;
		s_iCalled = 0;
		
		CQThread cCQThread = new CQThread();
		
		CTest.TEST_PERFORMANCE_BEGIN();
		
		cCQThread.startThread();
		
		for(int i=0; i<iPostCnt; i++)
		{
			cCQThread.postRequest(new TestRequest(i));
		}
		
		cCQThread.stopThread();
		
		CTest.EXPECT_TRUE(CTest.TEST_PERFORMANCE_END() < 1000);
		CTest.EXPECT_TRUE(iPostCnt == s_iCalled);
	}

	public static void main(String[] args) {
		
		CTest.ADD_TEST(TestCQThread.class);
		
		CTest.RUN_ALL_TESTS();
	}
}
