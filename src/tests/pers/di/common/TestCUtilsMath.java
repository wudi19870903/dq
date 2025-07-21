package pers.di.common;

import java.math.BigDecimal;

import pers.di.common.*;

public class TestCUtilsMath {
	
	@CTest.test
	public static void test_math45()
	{
		float pi=3.1415926f;
		int iTest = 0;
		for(int i=0; i<10000*10000; i++)
		{
			iTest += Math.round(pi);
		}
		CLog.debug("TEST", "iTest %d",iTest);
		
		float F1=3.1415926f;
		CLog.debug("TEST", "F1 %f",F1);
		
		float F2=3782.141f;
		CLog.debug("TEST", "F2 %f",F2);
		
		float F3=37825.141f;
		CLog.debug("TEST", "F3 %f",F3);
		
		double D1 = 37825.1416;
		CLog.debug("TEST", "D1 %.3f",D1);
		
//		float f1 = 10.782f;
//		int i1 = 3200;
//		double dT = (double)f1*(double)i1;
//		double dT2 = CUtilsMath.toDouble(f1)*CUtilsMath.toDouble(i1);
//		for(int i=0; i<10000*100; i++)
//		{
//			double x = BigDecimal.valueOf(10.782*3200).doubleValue();
//		}
//		CLog.output("TEST", "ft %.3f",BigDecimal.valueOf(10.782f*3200).doubleValue());
		
		{
			long lt= 34502400;
			double dt = lt/1000.0;
			CLog.debug("TEST", "ft %.3f",BigDecimal.valueOf(10.782f*3200).doubleValue());
		}
		
		//for(int i=0; i<10000*1000; i++)
		{
			//double res = CUtilsMath.multiply(f1, i1);
			//CLog.output("TEST", "res %.3f", res);
		}
		
		double f2 = 34502.400f;
		CLog.debug("TEST", "f2 %.3f",f2);
		
		double d2 = 34502.400;
		CLog.debug("TEST", "d2 %.3f",d2);
	}
	
	@CTest.test
	public static void test_math_cal()
	{
		{
			float f1=1.234567f;
			float S = 1.0f;
			for(int i=0; i<10000*10000; i++)
			{
				S = f1*S;
				if(S>99999) S=1.0f;
			}
			CLog.debug("TEST", "test_math_cal S %f", S);
		}
//		{
//			double d1=1.234567890123;
//			double S = 1.000000;
//			for(int i=0; i<10000*10000; i++)
//			{
//				S = d1*S;
//				if(S>99999) S=1.00000;
//			}
//			CLog.output("TEST", "test_math_cal S %f", S);
//		}
	}
	
	@CTest.test
	public static void test_ooo()
	{
		
		CLog.debug("TEST", "saveNDecimal %f\n", CUtilsMath.saveNDecimalIgnore(2.199f, 1));
		CLog.debug("TEST", "saveNDecimal %f\n", CUtilsMath.saveNDecimalIgnore(2.199f, 2));
		CLog.debug("TEST", "saveNDecimal %f\n", CUtilsMath.saveNDecimalIgnore(2.199f, 3));
		
		
		CLog.debug("TEST", "randomFloat %f\n", CUtilsMath.randomFloat());
		CLog.debug("TEST", "randomFloat %f\n", CUtilsMath.randomFloat());
		CLog.debug("TEST", "randomFloat %f\n", CUtilsMath.randomFloat());
		
		
		
		CLog.debug("TEST", "saveNDecimal45 %.3f = 9.19?\n", CUtilsMath.saveNDecimal(8.35f*1.1f, 2));
		CLog.debug("TEST", "saveNDecimal45 %.3f = 27.42?\n", CUtilsMath.saveNDecimal(24.93f*1.1f, 2));
		CLog.debug("TEST", "saveNDecimal45 %.3f = 30.16?\n", CUtilsMath.saveNDecimal(27.42f*1.1f, 2));
		CLog.debug("TEST", "saveNDecimal45 %.3f = 30.18?\n", CUtilsMath.saveNDecimal(30.16f*1.1f, 2));
		
	}
	
	@CTest.test
	public static void test_sqrt()
	{
		double res1 = CUtilsMath.sqrt(4,3);
		CTest.EXPECT_DOUBLE_EQ(res1, 1.5874010519681994, 3);
		
		double res2 = CUtilsMath.sqrt(3.18,7);
		CTest.EXPECT_DOUBLE_EQ(res2, 1.18, 2);
	}
	
	public static void main(String[] args) {
		CTest.ADD_TEST(TestCUtilsMath.class);
		CTest.RUN_ALL_TESTS();
	}
}
