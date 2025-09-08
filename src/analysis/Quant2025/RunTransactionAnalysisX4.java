package analysis.Quant2025;

import java.lang.invoke.MethodHandles;

import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAContext;
import pers.di.dquant.DQuant;
import pers.di.dquant.IStockPickStrategy;
import pers.di.dquant.TestDQuantTransaction.MyTestPickA;
import pers.di.dquant.internal.PickerReport;
import pers.di.model.KLine;

public class RunTransactionAnalysisX4 {
     public static void runTransactionAnalysis() {
        IStockPickStrategy instancePickStrategy = new RunPickAnalysisX4();
        DQuant.getInstance().runUserTransactionAnalysis(
            "HistoryTest 2020-01-01 2024-12-31", instancePickStrategy);
    }
    
    public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CLog.config_setTag("TEST", true);
        CLog.config_setTag("REPORT", true);
		CLog.config_setTag("ACCOUNT", false);
        CLog.config_setTag("DQUANT", true);
		runTransactionAnalysis();
		CSystem.stop();
	}
}
