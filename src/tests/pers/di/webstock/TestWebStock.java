package pers.di.webstock;

import java.util.ArrayList;
import java.util.List;

import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.common.CUtilsDateTime;
import pers.di.webstock.*;
import pers.di.webstock.IWebStock.*;
import pers.di.model.*;

public class TestWebStock {
	private static final boolean DEBUG_TESTCASE_LOG = false;
	private static void TESTCASE_LOG(String s) {
		if (DEBUG_TESTCASE_LOG) CLog.debug("TEST", s);
	}
	
	@CTest.test
	public void test_getDividendPayout()
	{
		{
			String StockID = "000488";
			List<DividendPayout> container = new ArrayList<DividendPayout>();
			int error = WebStock.instance().getDividendPayout(StockID, container);
			if(0 == WebStock.instance().getDividendPayout(StockID, container))
			{
				TESTCASE_LOG(String.format("DataWebStockDividendPayout.getDividendPayout %s OK!", StockID));
				for(int i = 0; i < container.size(); i++)  
		        {  
					DividendPayout cDividendPayout = container.get(i);  
					TESTCASE_LOG(String.format("%s %.1f %.1f %.1f",
							cDividendPayout.date,
							cDividendPayout.songGu,
							cDividendPayout.zhuanGu,
							cDividendPayout.paiXi));
		        } 
			}
			CTest.EXPECT_LONG_EQ(error, 0);
			CTest.EXPECT_TRUE(container.size() >= 18);
			CTest.EXPECT_DOUBLE_EQ(container.get(0).songGu, 1.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(0).zhuanGu, 0.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(0).paiXi, 3.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(1).songGu, 2.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(1).zhuanGu, 6.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(1).paiXi, 0.5);
		}
		{
			String StockID = "600056";
			List<DividendPayout> container = new ArrayList<DividendPayout>();
			int error = WebStock.instance().getDividendPayout(StockID, container);
			if(0 == WebStock.instance().getDividendPayout(StockID, container))
			{
				TESTCASE_LOG(String.format("DataWebStockDividendPayout.getDividendPayout %s OK!", StockID));
				for(int i = 0; i < container.size(); i++)  
		        {  
					DividendPayout cDividendPayout = container.get(i);  
					TESTCASE_LOG(String.format("%s %.1f %.1f %.1f",
							cDividendPayout.date,
							cDividendPayout.songGu,
							cDividendPayout.zhuanGu,
							cDividendPayout.paiXi));
		        } 
			}
			CTest.EXPECT_LONG_EQ(error, 0);
			CTest.EXPECT_TRUE(container.size() >= 18);
			CTest.EXPECT_DOUBLE_EQ(container.get(0).songGu, 0.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(0).zhuanGu, 0.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(0).paiXi, 4.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(7).songGu, 0.0);
			CTest.EXPECT_DOUBLE_EQ(container.get(7).zhuanGu, 4.91);
			CTest.EXPECT_DOUBLE_EQ(container.get(7).paiXi, 0.0);
		}
	}
	
	@CTest.test
	public void test_getKLine()
	{
		String stockID = "600056";
		List<KLine> ctnKLine = new ArrayList<KLine>();
		int error = WebStock.instance().getKLine(stockID, 2500, ctnKLine);
		if(0 == error)
		{
			TESTCASE_LOG("List<TradeDetail> size=" + ctnKLine.size());
			if(ctnKLine.size() > 11)
			{
				for(int i = 0; i < 5; i++)  
		        { 
					KLine cKLine = ctnKLine.get(i);  
		            TESTCASE_LOG(cKLine.date + "," 
		            		+ cKLine.open + "," + cKLine.close
		            		 + "," + cKLine.low + "," + cKLine.high);  
		        }
				TESTCASE_LOG("...");
				for(int i = ctnKLine.size()-5; i < ctnKLine.size(); i++)  
		        { 
					KLine cKLine = ctnKLine.get(i);  
		            TESTCASE_LOG(cKLine.date + "," 
		            		+ cKLine.open + "," + cKLine.close
		            		 + "," + cKLine.low + "," + cKLine.high);  
		        }
			}
			else
			{
				for(int i = 0; i < ctnKLine.size(); i++)  
		        {  
					KLine cKLine = ctnKLine.get(i);  
		            TESTCASE_LOG(cKLine.date + "," 
		            		+ cKLine.open + "," + cKLine.close
		            		 + "," + cKLine.low + "," + cKLine.high);  
		        }
			}
		}
		else
		{
			TESTCASE_LOG("ERROR:" + error);
		}
		TESTCASE_LOG("id:" + stockID + " kline.size:" + ctnKLine.size());
		CTest.EXPECT_LONG_EQ(error, 0);
		CTest.EXPECT_TRUE(ctnKLine.size() > 250);
		CTest.EXPECT_TRUE(hasDateInKLineList(ctnKLine, "2025-01-02"));
		KLine klinetest1 = getDateKLine(ctnKLine, "2025-01-02");
		CTest.EXPECT_TRUE(null != klinetest1);
		CTest.EXPECT_DOUBLE_EQ(klinetest1.open, 11.09);
		CTest.EXPECT_DOUBLE_EQ(klinetest1.close, 10.78);
	}

	private boolean hasDateInKLineList(List<KLine> ctnKLine, String dateStr) {
		boolean hasDate = false;
		for(int i = 0; i < ctnKLine.size(); i++)
		{
			if(ctnKLine.get(i).date.equals(dateStr))
			{
				hasDate = true;
				break;
			}
		}
		return hasDate;
	}
	private KLine getDateKLine(List<KLine> ctnKLine, String dateStr) {
		KLine cKLine = null;
		for(int i = 0; i < ctnKLine.size(); i++)
		{
			if(ctnKLine.get(i).date.equals(dateStr))
			{
				cKLine = ctnKLine.get(i);
				break;
			}
		}
		return cKLine;
	}

	public static void main(String[] args) {
		CSystem.start();
		CTest.ADD_TEST(TestWebStock.class);
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
