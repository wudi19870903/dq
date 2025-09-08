package pers.di.dquant;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAContext;
import pers.di.dataprovider.DataTestHelper;
import pers.di.dquant.internal.PickerReport;
import pers.di.model.KLine;

public class TestDQuantPick {
	private static final boolean DEBUG_TESTCASE_LOG = false;
	private static void TESTCASE_LOG(String format, Object... args) {
		if (DEBUG_TESTCASE_LOG) CLog.info("TEST", String.format(format, args));
	}
    public static List<String> stockIDs = new ArrayList<String>()
			{{add("999999");add("600000");;add("600056");add("300163");add("002468");}};

    @CTest.setup
    public void setup() {
        TESTCASE_LOG("TestDAContext.setup");
        String newestDate = "2025-01-02";
		DataTestHelper.InitLocalData(newestDate, stockIDs);
    }
    @CTest.teardown
    public void teardown() {
        TESTCASE_LOG("TestDAContext.teardown");
    }

    public static class MyTestPickerA implements IStockPickStrategy {
        public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
            if (kLines.size() < 5 || stockID.equals("999999")) {
                return false;
            }
            // 首次4连阳判断
            int iT = kLines.size() - 1;
            boolean currentIs4LianRed = kLines.get(iT).isRed() && kLines.get(iT-1).isRed() 
                && kLines.get(iT-2).isRed() && kLines.get(iT-3).isRed();
            boolean yestordayIs4LianRed = kLines.get(iT-1).isRed() && kLines.get(iT-2).isRed() 
                && kLines.get(iT-3).isRed() && kLines.get(iT-4).isRed();
            if(currentIs4LianRed && !yestordayIs4LianRed) {
                //CLog.info("TEST", "MyTestPickerA.onUserPick %s", stockID);
                return true;
            }
            
            return false;
        };
    }

    @CTest.test
	public static void test_runUserPickAnalysis() {
        PickerReport report = new PickerReport(20, 0.05, 0.15);
        DQuant.getInstance().runUserPickAnalysis("HistoryTest 2024-01-01 2024-06-30", new MyTestPickerA(), report);
        report.dump();
        CTest.EXPECT_TRUE(report.pickList.size() > 0);
        int pickCount = 0;
        for (int i = 0; i < report.pickList.size(); i++) { 
            Entry<String, String> pickItem = report.pickList.get(i);
            String date = pickItem.getKey();
            String stockID = pickItem.getValue();
            TESTCASE_LOG("PickerReport pickItem %s %s", date, stockID);
            if (stockIDs.indexOf(stockID) >= 0) {
                pickCount++;
            }
        }
        CTest.EXPECT_LONG_EQ(pickCount, 11);
        CTest.EXPECT_LONG_EQ(report.pickKLineMap.size(), 11);
        CTest.EXPECT_LONG_EQ(report.shortWinMap.size(), 4);
        CTest.EXPECT_STR_EQ(report.shortWinMap.get(new java.util.AbstractMap.SimpleEntry<>("2024-02-21", "300163")),
            "2024-02-23");
        CTest.EXPECT_STR_EQ(report.shortWinMap.get(new java.util.AbstractMap.SimpleEntry<>("2024-02-23", "002468")),
            "2024-03-20");
        CTest.EXPECT_LONG_EQ(report.shortLoseMap.size(), 2);
        CTest.EXPECT_STR_EQ(report.shortLoseMap.get(new java.util.AbstractMap.SimpleEntry<>("2024-04-02", "300163")),
            "2024-04-15");
        CTest.EXPECT_TRUE(Math.abs(report.getShortWinRate() - 0.363636363636) < 0.0001);
    }

    public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CLog.config_setTag("TEST", true);
		CLog.config_setTag("REPORT", true);
        CLog.config_setTag("ACCOUNT", false);
		CTest.ADD_TEST(TestDQuantPick.class);
		CTest.RUN_ALL_TESTS("");
		CLog.debug("TEST", "END");
		CSystem.stop();
	}
}
