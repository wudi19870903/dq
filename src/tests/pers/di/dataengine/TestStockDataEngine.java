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
import pers.di.dataprovider.DataTestHelper;
import pers.di.model.KLine;
import pers.di.model.TimePrice;

public class TestStockDataEngine {
    private static final boolean DEBUG_TESTCASE_LOG = false;
	private static void TESTCASE_LOG(String format, Object... args) {
		if (DEBUG_TESTCASE_LOG) CLog.info("TEST", String.format(format, args));
	}
	public static List<String> stockIDs = new ArrayList<String>()
		{{add("999999");add("600000");;add("600056");add("300163");add("002468");}};

    public static int s_testCount_listenerX = 0;
	public static int s_testCount_listenerY = 0;

    @CTest.setup
    public void setup() {
        TESTCASE_LOG("TestStockDataEngine.setup");
		String newestDate = "2025-01-02";
		DataTestHelper.InitLocalData(newestDate, stockIDs);
    }
    @CTest.teardown
    public void teardown() {
        TESTCASE_LOG("TestStockDataEngine.teardown");
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
				KLine cCurrentKLine =  cDAKLines.end();
				TESTCASE_LOG("AllStockCnt:%d ID:%s ALLKLineSize:%d Date:%s Open:%.3f Close:%.3f", 
						context.getAllStockID().size(),
						stockID,
						cDAKLines.size(),
						cCurrentKLine.date,
						cCurrentKLine.open,
						cCurrentKLine.close);
				testDayCount++;
				if(cCurrentKLine.date.equals("2024-09-02"))
				{
					CTest.EXPECT_STR_EQ(context.date(), cCurrentKLine.date); 
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.open, 1.59, 2);
					s_testCount_listenerX++;
				}
				if(cCurrentKLine.date.equals("2024-12-11"))
				{
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.close, 2.79, 2);
					CTest.EXPECT_LONG_EQ(testDayCount, 66);
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
			if(context.date().equals("2024-09-30"))
			{
				String stockID = "600056";
				CListObserver<KLine> cDAKLines = context.getDayKLines(stockID);
				if(cDAKLines.size() > 0)
				{
					KLine cCurrentKLine =  cDAKLines.end();
					TESTCASE_LOG("AllStockCnt:%d ID:%s ALLKLineSize:%d Date:%s O:%.3f C:%.3f L:%.3f H:%.3f", 
							context.getAllStockID().size(),
							stockID,
							cDAKLines.size(),
							cCurrentKLine.date,
							cCurrentKLine.open,
							cCurrentKLine.close,
							cCurrentKLine.low,
							cCurrentKLine.high);
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.low, 10.92, 2);
					s_testCount_listenerY++;
				}
			}
			if(context.date().equals("2025-01-02"))
			{
				String stockID = "600056";
				CListObserver<KLine> cDAKLines = context.getDayKLines(stockID);
				if(cDAKLines.size() > 0)
				{
					KLine cCurrentKLine =  cDAKLines.end();
					TESTCASE_LOG( "AllStockCnt:%d ID:%s ALLKLineSize:%d Date:%s O:%.3f C:%.3f L:%.3f H:%.3f", 
							context.getAllStockID().size(),
							stockID,
							cDAKLines.size(),
							cCurrentKLine.date,
							cCurrentKLine.open,
							cCurrentKLine.close,
							cCurrentKLine.low,
							cCurrentKLine.high);
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.open, 11.00, 2);
					CTest.EXPECT_DOUBLE_EQ(cCurrentKLine.close, 10.69, 2);
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
		
		StockDataEngine.getInstance().config("TriggerMode", "HistoryTest 2024-09-01 2025-01-02");
		StockDataEngine.getInstance().registerListener(cEngineListenerTesterX);
		StockDataEngine.getInstance().registerListener(cEngineListenerTesterY);
		StockDataEngine.getInstance().run();
		
		CTest.EXPECT_LONG_EQ(s_testCount_listenerX, 2);
		CTest.EXPECT_LONG_EQ(s_testCount_listenerY, 4);
	}

	public static void main(String[] args) {
		CSystem.start();
		CTest.ADD_TEST(TestStockDataEngine.class);
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
