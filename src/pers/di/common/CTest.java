package pers.di.common;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

public abstract class CTest {
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface test {
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface setup {
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface teardown {
	}
	
	public static void TEST_PERFORMANCE_BEGIN()
	{
		s_curTestPerformanceTCB = System.currentTimeMillis();
	}
	
	public static long TEST_PERFORMANCE_END()
	{
		long TCE = System.currentTimeMillis();
		return TCE-s_curTestPerformanceTCB;
	}
	
	public static void EXPECT_NULL(Object bActual)
	{
		if(null != bActual) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        outputProcess("[CTEST] NG(%d) expect(null) actual(object) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static void EXPECT_NOT_NULL(Object bActual)
	{
		if(null == bActual) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        outputProcess("[CTEST] NG(%d) expect(null) actual(object) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static void EXPECT_TRUE(boolean bActual)
	{
		if(!bActual) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        outputProcess("[CTEST] NG(%d) expect(true) actual(%b) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		bActual, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static void EXPECT_FALSE(boolean bActual)
	{
		if(bActual) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        outputProcess("[CTEST] NG(%d) expect(false) actual(%b) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		bActual, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static void EXPECT_STR_EQ(String sActual, String sExpect)
	{
		if(!sExpect.equals(sActual)) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        outputProcess("[CTEST] NG(%d) expect(%s) actual(%s) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		sExpect, sActual, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static void EXPECT_STR_NE(String sActual, String sExpect)
	{
		if(sExpect.equals(sActual)) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        outputProcess("[CTEST] NG(%d) !expect(%s) actual(%s) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		sExpect, sActual, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static void EXPECT_LONG_EQ(long lActual, long lExpect)
	{
		if(lExpect != lActual) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        outputProcess("[CTEST] NG(%d) expect(%d) actual(%d) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		lExpect, lActual, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static void EXPECT_LONG_NE(long lActual, long lExpect)
	{
		if(lExpect == lActual) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        outputProcess("[CTEST] NG(%d) !expect(%d) actual(%d) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		lExpect, lActual, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static void EXPECT_DOUBLE_EQ(double dActual, double dExpect, int precision)
	{
		DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(precision);
        String dExpectStr = df.format(dExpect);
        String dActualStr = df.format(dActual);
		if(!dActualStr.equals(dExpectStr)) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];

	        outputProcess("[CTEST] NG(%d) expect(%s) actual(%s) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		dExpectStr, dActualStr, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	public static void EXPECT_DOUBLE_EQ(double dActual, double dExpect)
	{
		if(0 != Double.compare(dActual, dExpect)) 
		{
			s_curTestInnnerErrorCount++;
			
			StackTraceElement[] trace = new Throwable().getStackTrace();
	        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
	        StackTraceElement tmp = trace[1];
	        
	        DecimalFormat df = new DecimalFormat();
	        df.setMaximumFractionDigits(12);
	        String dExpectStr = df.format(dExpect);
	        String dActualStr = df.format(dActual);
	        
	        outputProcess("[CTEST] NG(%d) expect(%s) actual(%s) (%s:%d)",
	        		s_curTestInnnerErrorCount, 
	        		dExpectStr, dActualStr, 
	        		tmp.getFileName(), tmp.getLineNumber());
		}
	}
	
	public static int ADD_TEST(Class<?> cls)
	{
		try {
			s_clsList.add(cls);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public static int RUN_ALL_TESTS(String filter)
	{
		outputProcess("[CTEST] RUN_ALL_TESTS BEGIN");
		
		for(int iCls = 0; iCls<s_clsList.size(); iCls++)
		{
			Class<?> testCls = s_clsList.get(iCls);
			String clsName = testCls.getName();
			String clsSimpleName = testCls.getSimpleName();
			//outputProcess("TestClass: %s", clsSimpleName);
			
			try {
				
				Object obj = testCls.newInstance();
				Method[] methods = testCls.getDeclaredMethods();
				
				// call setup
				for(int iMethod = 0; iMethod < methods.length; iMethod++)
				{
					Method method = methods[iMethod];
					
					boolean bCallTestSetup = false;
					String methodName = method.getName();
					Annotation[] allAnnotations = method.getAnnotations();
					for(Annotation an : allAnnotations)
					{
				        Class annotationType = an.annotationType();
				        if(annotationType.getName().equals(setup.class.getName())
				        		&& methodName.equals("setup"))
				        {
				        	bCallTestSetup = true;
				        }
					}
					
					if(bCallTestSetup)
					{
						method.invoke(obj);
					}
				}
				
				// call test
				for(int iMethod = 0; iMethod < methods.length; iMethod++)
				{
					Method method = methods[iMethod];
					
					//outputProcess("    method: %s", methodName);
					
					boolean bCallTest = false;
					String methodName = method.getName();
					Annotation[] allAnnotations = method.getAnnotations();
					for(Annotation an : allAnnotations)
					{
				        Class annotationType = an.annotationType();
				        //outputProcess("        annotationType: %s", annotationType.getName());
				        
				        if(annotationType.getName().equals(test.class.getName()))
				        {
				        	bCallTest = true;
				        }
					}
					if(bCallTest 
							&& null != filter 
							&& !filter.equals(""))
					{
						String testName= clsSimpleName + "." + methodName;
						if(testName.contains(filter))
						{
							bCallTest = true;
						}
						else
						{
							bCallTest = false;
						}
					}

					// call test
					if(bCallTest)
					{
						outputProcess("[CTEST] %s.%s", clsSimpleName, methodName);
						s_curTestInnnerErrorCount = 0;
						s_curTestInnnerTCB = System.currentTimeMillis();
						method.invoke(obj);
						long TCE = System.currentTimeMillis();
						if(0 == s_curTestInnnerErrorCount)
						{
							outputProcess("[CTEST] %s.%s [%dms][OK]", clsSimpleName, methodName, TCE-s_curTestInnnerTCB);
						}
						else
						{
							outputProcess("[CTEST] %s.%s [%dms][NG]", clsSimpleName, methodName, TCE-s_curTestInnnerTCB);
							s_testErrorCount++;
						}
					}
				}
				
				// call teardown
				for(int iMethod = 0; iMethod < methods.length; iMethod++)
				{
					Method method = methods[iMethod];
					
					boolean bCallTestTeardown = false;
					String methodName = method.getName();
					Annotation[] allAnnotations = method.getAnnotations();
					for(Annotation an : allAnnotations)
					{
				        Class annotationType = an.annotationType();
				        if(annotationType.getName().equals(teardown.class.getName())
				        		&& methodName.equals("teardown"))
				        {
				        	bCallTestTeardown = true;
				        }
					}
					
					if(bCallTestTeardown)
					{
						method.invoke(obj);
					}
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		outputProcess("[CTEST] RUN_ALL_TESTS END Error(%d)", s_testErrorCount);
		return s_testErrorCount;
	}
	
	public static int RUN_ALL_TESTS()
	{
		return RUN_ALL_TESTS("");
	}
	
	public static void outputLog(String format, Object... args) {
		String logstr = String.format(format, args);
		System.out.println(logstr);
	}
	
	private static long s_curTestPerformanceTCB = 0;
	private static int s_curTestInnnerErrorCount = 0;
	private static long s_curTestInnnerTCB = 0;
	
	private static int s_testErrorCount = 0;
	private static List<Class<?>> s_clsList = new ArrayList<Class<?>>();
	
	/**
	 * ****************************************************************************************
	 * LOG
	 */
	
	private static void outputProcess(String format, Object... args)
	{
		String logstr = String.format(format, args);
		//s_fmt.format("%s\n", logstr);
		CLog.info("TEST", logstr);
	}
	private static Formatter s_fmt = new Formatter(System.out);
}
