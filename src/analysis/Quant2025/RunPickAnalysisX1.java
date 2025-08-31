package analysis.Quant2025;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAContext;
import pers.di.dquant.DQuant;
import pers.di.dquant.IStockPickStrategy;
import pers.di.dquant.PickerReport;
import pers.di.model.KLine;

public class RunPickAnalysisX1 {
    public static class MyPickerX1 extends IStockPickStrategy {
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
	public static void runPickAnalysis() {
        PickerReport report = new PickerReport();
        DQuant.getInstance().runUserPickAnalysis("HistoryTest 2023-09-01 2024-01-02",
            new MyPickerX1(), 
            report);
        report.dump();
    }

    public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CLog.config_setTag("TEST", true);
        CLog.config_setTag("REPORT", true);
		CLog.config_setTag("ACCOUNT", false);
		runPickAnalysis();
		CSystem.stop();
	}
}
