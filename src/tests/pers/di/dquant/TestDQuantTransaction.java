package pers.di.dquant;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAContext;
import pers.di.dataprovider.DataTestHelper;
import pers.di.model.KLine;

public class TestDQuantTransaction {
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

    public static class MyTestPickA implements IStockPickStrategy {
        @Override
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
        }
    }

    @CTest.test
	public static void test_runUserTransactionAnalysis() {
        DQuant.getInstance().runUserTransactionAnalysis(
            "HistoryTest 2024-09-01 2024-12-31", new MyTestPickA());
    }

    public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CLog.config_setTag("TEST", true);
		CLog.config_setTag("REPORT", false);
        CLog.config_setTag("ACCOUNT", false);
        CLog.config_setTag("DQUANT", true);
		CTest.ADD_TEST(MethodHandles.lookup().lookupClass());
		CTest.RUN_ALL_TESTS("");
		CSystem.stop();
	}
}
