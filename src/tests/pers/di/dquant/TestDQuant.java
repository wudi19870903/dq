package pers.di.dquant;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAContext;
import pers.di.dataprovider.DataTestHelper;
import pers.di.model.KLine;

public class TestDQuant {
    public static List<String> stockIDs = new ArrayList<String>()
			{{add("999999");add("600000");;add("600056");add("300163");add("002468");}};

    @CTest.setup
    public void setup() {
        CLog.info("TestDAContext.setup");
        String newestDate = "2025-01-02";
		DataTestHelper.InitLocalData(newestDate, stockIDs);
    }
    @CTest.teardown
    public void teardown() {
        CLog.info("TestDAContext.teardown");
    }

    public static class MyTestPickerA extends IStockPickStrategy {
        public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
            if (kLines.size() < 5) {
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
        PickerReport report = new PickerReport();
        DQuant.getInstance().runUserPickAnalysis("HistoryTest 2024-09-01 2025-01-02", new MyTestPickerA(), report);
        report.dump();
        CTest.EXPECT_TRUE(report.pickList.size() > 0);
        int pickCount = 0;
        for (int i = 0; i < report.pickList.size(); i++) { 
            Pair<String, String> pickItem = report.pickList.get(i);
            String date = pickItem.getKey();
            String stockID = pickItem.getValue();
            CLog.info("TEST", "PickerReport pickItem %s %s", date, stockID);
            if (stockIDs.indexOf(stockID) >= 0) {
                pickCount++;
            }
        }
        CTest.EXPECT_LONG_EQ(pickCount, 10);
        CTest.EXPECT_LONG_EQ(report.pickKLineMap.size(), 10);
        CTest.EXPECT_LONG_EQ(report.shortWinMap.size(), 7);
        CTest.EXPECT_STR_EQ(report.shortWinMap.get(new Pair<String, String>("2024-09-24", "600000")),
            "2024-09-26");
        CTest.EXPECT_STR_EQ(report.shortWinMap.get(new Pair<String, String>("2024-11-05", "600056")),
            "2024-11-12");
        CTest.EXPECT_LONG_EQ(report.shortLoseMap.size(), 1);
        CTest.EXPECT_STR_EQ(report.shortLoseMap.get(new Pair<String, String>("2024-12-09", "300163")),
            "2024-12-17");
        CTest.EXPECT_DOUBLE_EQ(report.getShortWinRate(), 0.7);
    }

    public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CLog.config_setTag("TEST", true);
		CLog.config_setTag("REPORT", true);
        CLog.config_setTag("ACCOUNT", false);
		CTest.ADD_TEST(TestDQuant.class);
		CTest.RUN_ALL_TESTS("");
		CLog.debug("TEST", "END");
		CSystem.stop();
	}
}
