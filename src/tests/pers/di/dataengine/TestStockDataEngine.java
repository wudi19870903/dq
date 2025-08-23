package pers.di.dataengine;
import java.util.ArrayList;
import java.util.List;

import pers.di.common.CFileSystem;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.common.CUtilsMath;
import pers.di.dataprovider.DataProvider;
import pers.di.model.KLine;
import pers.di.model.TimePrice;

public class TestStockDataEngine {
    public static int s_testCount_listenerX = 0;
	public static int s_testCount_listenerY = 0;

    @CTest.setup
    public void setup() {
        CLog.info("TestStockDataEngine.setup");
    }
    @CTest.teardown
    public void teardown() {
        CLog.info("TestStockDataEngine.teardown");
    }

    public static class EngineListenerTesterX extends IEngineListener
	{
		public int testDayCount = 0;
		@Override
		public void onTradingDayFinish(DAContext context)
		{
			String stockID = "300163";
			CListObserver<KLine> cDAKLines = context.getDayKLines(stockID);
			if(cDAKLines.size() > 0)
			{
				KLine cCurrentKLine =  cDAKLines.get(cDAKLines.size()-1);
//				CLog.output("TEST", "AllStockCnt:%d ID:%s ALLKLineSize:%d Date:%s Open:%.3f Close:%.3f", 
//						context.pool().size(),
//						stockID,
//						cDAKLines.size(),
//						cCurrentKLine.date,
//						cCurrentKLine.open,
//						cCurrentKLine.close);
				testDayCount++;
				if(cCurrentKLine.date.equals("2017-01-03"))
				{
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.open, 10.22, 2);
					s_testCount_listenerX++;
				}
				if(cCurrentKLine.date.equals("2017-02-03"))
				{
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.close, 10.12, 2);
					CTest.EXPECT_LONG_EQ(testDayCount, 1466);
					s_testCount_listenerX++;
				}
			}
		}
	}
	
	public static class EngineListenerTesterY extends IEngineListener
	{
		private String stockID = "600056";
		
		@Override
		public void onInitialize(DAContext context)
		{
			s_testCount_listenerY++;
		}
		@Override
		public void onUnInitialize(DAContext context)
		{
			s_testCount_listenerY++;
		}
		
		
		@Override
		public void onTradingDayFinish(DAContext context)
		{
			if(context.date().equals("2004-10-22"))
			{
				String stockID = "600056";
				CListObserver<KLine> cDAKLines = context.getDayKLines(stockID);
				if(cDAKLines.size() > 0)
				{
					KLine cCurrentKLine =  cDAKLines.get(cDAKLines.size()-1);
//					CLog.output("TEST", "AllStockCnt:%d ID:%s ALLKLineSize:%d Date:%s O:%.3f C:%.3f L:%.3f H:%.3f", 
//							context.pool().size(),
//							stockID,
//							cDAKLines.size(),
//							cCurrentKLine.date,
//							cCurrentKLine.open,
//							cCurrentKLine.close,
//							cCurrentKLine.low,
//							cCurrentKLine.high);
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.low, -1.185, 2);
					s_testCount_listenerY++;
				}
			}
			if(context.date().equals("2007-11-12"))
			{
				String stockID = "600056";
				CListObserver<KLine> cDAKLines = context.getDayKLines(stockID);
				if(cDAKLines.size() > 0)
				{
					KLine cCurrentKLine =  cDAKLines.get(cDAKLines.size()-1);
//					CLog.output("TEST", "AllStockCnt:%d ID:%s ALLKLineSize:%d Date:%s O:%.3f C:%.3f L:%.3f H:%.3f", 
//							context.pool().size(),
//							stockID,
//							cDAKLines.size(),
//							cCurrentKLine.date,
//							cCurrentKLine.open,
//							cCurrentKLine.close,
//							cCurrentKLine.low,
//							cCurrentKLine.high);
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.open, 4.58, 2);
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.close, 4.52, 2);
					s_testCount_listenerY++;
				}
			}
		}
	}
    @CTest.test
	public void test_StockDataEngine_History()
	{
		EngineListenerTesterX cEngineListenerTesterX = new EngineListenerTesterX();
		EngineListenerTesterY cEngineListenerTesterY = new EngineListenerTesterY();
		
		//StockDataEngine.instance().config("TriggerMode", "HistoryTest 2004-01-01 2017-02-03");
		StockDataEngine.getInstance().config("TriggerMode", "HistoryTest 2004-09-01 2017-12-30");
		StockDataEngine.getInstance().registerListener(cEngineListenerTesterX);
		StockDataEngine.getInstance().registerListener(cEngineListenerTesterY);
		StockDataEngine.getInstance().run();
		
		CTest.EXPECT_LONG_EQ(s_testCount_listenerX, 2);
		CTest.EXPECT_LONG_EQ(s_testCount_listenerY, 5 + 242);
	}
}
