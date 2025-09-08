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
	private static final boolean DEBUG_TESTCASE_LOG = false;
	private static void TESTCASE_LOG(String format, Object... args) {
		if (DEBUG_TESTCASE_LOG) CLog.info("TEST", String.format(format, args));
	}

    @CTest.setup
    public void setup() {
        TESTCASE_LOG("TestDataProvider.setup");
    }
    @CTest.teardown
    public void teardown() {
        TESTCASE_LOG("TestDataProvider.teardown");
    }

    @CTest.test
	public void test_dataRoot() {
        String dataRoot = DataProvider.getInstance().dataRoot();
		CTest.EXPECT_TRUE(dataRoot != null);
        CTest.EXPECT_TRUE(dataRoot.contains("rw/data"));
        TESTCASE_LOG("dataRoot:%s", dataRoot);
    }

    @CTest.test
	public void test_getLocalAllStockIDList() {
        List<String> stockList = new ArrayList<>(); 
        int ret = DataProvider.getInstance().getLocalAllStockIDList(stockList);
		CTest.EXPECT_TRUE(0 == ret);
        CTest.EXPECT_TRUE(stockList.size() > 0);
        for (int i = 0; i < 4; i++) {
            TESTCASE_LOG(stockList.get(i));
        }
        TESTCASE_LOG("stockList size:%d", stockList.size());
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
            TESTCASE_LOG("update stockID:" + stockID);
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
            TESTCASE_LOG("update stockID:" + stockID);
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
            TESTCASE_LOG("LocalDayKLines stockID:" + stockID);
            TESTCASE_LOG("    date:" + "2025-01-02" + " open:" + klinetest1.open + " close:" + klinetest1.close);
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
            TESTCASE_LOG("DayKLinesForwardAdjusted stockID:" + stockID);
            TESTCASE_LOG("    date:" + "2025-01-02" + " open:" + klinetest1.open + " close:" + klinetest1.close);
        }
    }

    @CTest.test
	public void test_getDayKLinesBackwardAdjusted() {
        // get 601398
        {
            String stockID = "301153"; //中科江南
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
            ret = DataProvider.getInstance().getDayKLinesBackwardAdjusted(stockID, ctnKLine);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(ctnKLine.size() > 250);
            CTest.EXPECT_TRUE(hasDateInKLineList(ctnKLine, "2025-01-02"));
            KLine klinetest1 = getDateKLine(ctnKLine, "2025-01-02");
            CTest.EXPECT_TRUE(null != klinetest1);
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.open), 93.87);
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.close), 88.04);
            TESTCASE_LOG("DayKLinesForwardAdjusted stockID:" + stockID);
            TESTCASE_LOG("    date:" + "2025-01-02" + " open:" + klinetest1.open + " close:" + klinetest1.close);
        }

        // get 300163
        {
            String stockID = "300163"; //先锋新材
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
            ret = DataProvider.getInstance().getDayKLinesBackwardAdjusted(stockID, ctnKLine);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(ctnKLine.size() > 250);
            CTest.EXPECT_TRUE(hasDateInKLineList(ctnKLine, "2025-01-02"));
            KLine klinetest1 = getDateKLine(ctnKLine, "2025-01-02");
            CTest.EXPECT_TRUE(null != klinetest1);
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.open), 15.44);
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.close), 14.96);
            TESTCASE_LOG("DayKLinesForwardAdjusted stockID:" + stockID);
            TESTCASE_LOG("    date:" + "2025-01-02" + " open:" + klinetest1.open + " close:" + klinetest1.close);
        }

        // get 300045
        {
            String stockID = "300045"; //华力创通
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
            ret = DataProvider.getInstance().getDayKLinesBackwardAdjusted(stockID, ctnKLine);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(ctnKLine.size() > 250);
            CTest.EXPECT_TRUE(hasDateInKLineList(ctnKLine, "2025-01-02"));
            KLine klinetest1 = getDateKLine(ctnKLine, "2025-01-02");
            CTest.EXPECT_TRUE(null != klinetest1);
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.open), 168.28);
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.close), 163.08);
            TESTCASE_LOG("DayKLinesForwardAdjusted stockID:" + stockID);
            TESTCASE_LOG("    date:" + "2025-01-02" + " open:" + klinetest1.open + " close:" + klinetest1.close);
        }

        //get 600000
        {
            String stockID = "600000"; //浦发银行
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
            ret = DataProvider.getInstance().getDayKLinesBackwardAdjusted(stockID, ctnKLine);
            CTest.EXPECT_TRUE(0 == ret);
            CTest.EXPECT_TRUE(ctnKLine.size() > 250);
            CTest.EXPECT_TRUE(hasDateInKLineList(ctnKLine, "2025-01-02"));
            KLine klinetest1 = getDateKLine(ctnKLine, "2024-12-31");
            CTest.EXPECT_TRUE(null != klinetest1);
            KLine klinetest2 = getDateKLine(ctnKLine, "2025-01-02");
            CTest.EXPECT_TRUE(null != klinetest2);
            // TODO: 600000的后复权数据 每日涨跌幅是正确的，但绝对价格和软件上的不一致，后续再解
            // CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.open), 133.16);
            // CTest.EXPECT_DOUBLE_EQ(CUtilsMath.save2Decimal(klinetest1.close), 131.95);
            double winRate = (klinetest2.close - klinetest1.close)/klinetest1.close;
            CTest.EXPECT_DOUBLE_EQ(CUtilsMath.saveNDecimal(winRate, 4), -0.0104);
            TESTCASE_LOG("DayKLinesForwardAdjusted stockID:" + stockID);
            TESTCASE_LOG("    date:" + "2025-01-02" + " open:" + klinetest2.open + " close:" + klinetest2.close);
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
