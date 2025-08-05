package pers.di.dataprovider;

import java.util.ArrayList;
import java.util.List;

import pers.di.common.CFileSystem;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.common.CUtilsMath;
import pers.di.model.KLine;

public class TestDataProvider {
    @CTest.setup
    public void setup() {
        CLog.info("TestDataProvider.setup");
    }
    @CTest.teardown
    public void teardown() {
        CLog.info("TestDataProvider.teardown");
    }

    @CTest.test
	public void test_dataRoot() {
        String dataRoot = DataProvider.getInstance().dataRoot();
		CTest.EXPECT_TRUE(dataRoot != null);
        CTest.EXPECT_TRUE(dataRoot.contains("rw/data"));
        CLog.info("dataRoot:%s", dataRoot);
    }

    @CTest.test
	public void test_getLocalAllStockIDList() {
        List<String> stockList = new ArrayList<>(); 
        int ret = DataProvider.getInstance().getLocalAllStockIDList(stockList);
		CTest.EXPECT_TRUE(0 == ret);
        CTest.EXPECT_TRUE(stockList.size() > 0);
        for (int i = 0; i < 4; i++) {
            System.out.println(stockList.get(i));
        }
        CLog.info("stockList size:%d", stockList.size());
    }
    @CTest.test
	public void test_updateOneLocalStocks() {
        {
            String stockID = "601398"; //工商银行

            // clear
            String dateDir = DataProvider.getInstance().dataRoot() + "\\" + stockID;
            CFileSystem.removeDir(dateDir);
            CTest.EXPECT_FALSE(CFileSystem.isDirExist(dateDir));

            int ret = DataProvider.getInstance().updateOneLocalStocks(stockID);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(CFileSystem.isDirExist(dateDir));
            CLog.info("update stockID:" + stockID);
        }
        {
            String stockID = "920819"; //颖泰生物

            // clear
            String dateDir = DataProvider.getInstance().dataRoot() + "\\" + stockID;
            CFileSystem.removeDir(dateDir);
            CTest.EXPECT_FALSE(CFileSystem.isDirExist(dateDir));

            int ret = DataProvider.getInstance().updateOneLocalStocks(stockID);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(CFileSystem.isDirExist(dateDir));
            CLog.info("update stockID:" + stockID);
        }

    }

    @CTest.test
	public void test_getLocalDayKLines() {
        // get 601398
        {
            String stockID = "601398"; //工商银行
            // clear
            String dateDir = DataProvider.getInstance().dataRoot() + "\\" + stockID;
            CFileSystem.removeDir(dateDir);
            CTest.EXPECT_FALSE(CFileSystem.isDirExist(dateDir));
            // update
            int ret = DataProvider.getInstance().updateOneLocalStocks(stockID);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(CFileSystem.isDirExist(dateDir));
            // check
            List<KLine> ctnKLine = new ArrayList<KLine>();
            ret = DataProvider.getInstance().getLocalDayKLines(stockID, ctnKLine);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(ctnKLine.size() > 250);
            CTest.EXPECT_TRUE(hasDateInKLineList(ctnKLine, "2025-01-02"));
            KLine klinetest1 = getDateKLine(ctnKLine, "2025-01-02");
            CTest.EXPECT_TRUE(null != klinetest1);
            CTest.EXPECT_DOUBLE_EQ(klinetest1.open, 6.91);
            CTest.EXPECT_DOUBLE_EQ(klinetest1.close, 6.80);
            CLog.info("LocalDayKLines stockID:" + stockID);
            CLog.info("    date:" + "2025-01-02" + " open:" + klinetest1.open + " close:" + klinetest1.close);
        }
    }

    @CTest.test
	public void test_getDayKLinesForwardAdjusted() {
        // get 601398
        {
            String stockID = "601398"; //工商银行
            // clear
            String dateDir = DataProvider.getInstance().dataRoot() + "\\" + stockID;
            CFileSystem.removeDir(dateDir);
            CTest.EXPECT_FALSE(CFileSystem.isDirExist(dateDir));
            // update
            int ret = DataProvider.getInstance().updateOneLocalStocks(stockID);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(CFileSystem.isDirExist(dateDir));
            // check
            List<KLine> ctnKLine = new ArrayList<KLine>();
            ret = DataProvider.getInstance().getDayKLinesForwardAdjusted(stockID, ctnKLine);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(ctnKLine.size() > 250);
            CTest.EXPECT_TRUE(hasDateInKLineList(ctnKLine, "2025-01-02"));
            KLine klinetest1 = getDateKLine(ctnKLine, "2025-01-02");
            CTest.EXPECT_TRUE(null != klinetest1);
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.open), 6.6);
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.close), 6.49);
            CLog.info("DayKLinesForwardAdjusted stockID:" + stockID);
            CLog.info("    date:" + "2025-01-02" + " open:" + klinetest1.open + " close:" + klinetest1.close);
        }
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
		CTest.ADD_TEST(TestDataProvider.class);
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
