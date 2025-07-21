package pers.di.common;

import java.util.*;

import pers.di.common.CLRUMapCache;
import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestCLRUMapCache {
	
	@CTest.test
	public static void test_CLRUMapCache()
	{
		CLRUMapCache<String, String> cache = new CLRUMapCache<String, String>(5);
		
		cache.put("key1", "value1");
		cache.put("key2", "value2");
		cache.put("key3", "value3");
		cache.put("key4", "value4");
		cache.put("key5", "value5");
		
		CTest.EXPECT_STR_EQ(cache.get("key1"), "value1");
		
		CTest.EXPECT_TRUE(cache.containsKey("key2"));
		
		cache.put("key6", "value6");
		
		CTest.EXPECT_FALSE(cache.containsKey("key2"));
		CTest.EXPECT_STR_NE(cache.get("key2"), "value2");
	}
	
	@CTest.test
	public static void test_CLRUMapCache_performance()
	{
		CLRUMapCache<String, String> cache = new CLRUMapCache<String, String>(10000);
		
		for(int i=0; i<10000; i++)
		{
			String key = String.format("key_%s", i);
			String value = String.format("value_%s", i);
			cache.put(key, value);
		}
		for(int i=0; i<10000; i++)
		{
			String expect_value = String.format("value_%s", i);
			
			String key = String.format("key_%s", i);
			String value = cache.get(key);
			CTest.EXPECT_STR_EQ(value, expect_value);
		}
		
	}
	
	@CTest.test
	public static void test_HashMap_performance()
	{
		Map<String, String> cache = new HashMap<String, String>();
		
		for(int i=0; i<10000; i++)
		{
			String key = String.format("key_%s", i);
			String value = String.format("value_%s", i);
			cache.put(key, value);
		}
		for(int i=0; i<10000; i++)
		{
			String expect_value = String.format("value_%s", i);
			
			String key = String.format("key_%s", i);
			String value = cache.get(key);
			CTest.EXPECT_STR_EQ(value, expect_value);
		}
	}

	public static void main(String[] args) {
		CSystem.start();
		CTest.ADD_TEST(TestCLRUMapCache.class);
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
