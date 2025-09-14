package analysis.Quant2025;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import analysis.Common.GoldCrossChecker;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAContext;
import pers.di.dquant.DQuant;
import pers.di.dquant.IStockPickStrategy;
import pers.di.dquant.internal.PickerReport;
import pers.di.model.KLine;

/*
 * 中期创新高后的极速下跌形态检测

下面是一个Java实现，用于判断今日是否符合"中期创新高后的极速下跌"形态。这种形态通常表示股票在中期内创出新高后，突然出现快速下跌。

 */
public class RunPickAnalysisX10 implements IStockPickStrategy {
     @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        RunPickAnalysisX4 cRunPickAnalysisX4 = new RunPickAnalysisX4();
        RunPickAnalysisX5 cRunPickAnalysisX5 = new RunPickAnalysisX5();
        return cRunPickAnalysisX4.onUserPick(context, stockID, kLines)
            || cRunPickAnalysisX5.onUserPick(context, stockID, kLines);
    };

    public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CLog.config_setTag("TEST", true);
        CLog.config_setTag("REPORT", true);
		CLog.config_setTag("ACCOUNT", false);
        CLog.config_setTag("DQUANT", true);
		
        // 动态创建本类的选股策略实例
        IStockPickStrategy instancePickStrategy = null;
        Class<?> clazz = MethodHandles.lookup().lookupClass();
        try {
            instancePickStrategy = (IStockPickStrategy) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 运行选股策略,输出选股结果
        PickerReport report = new PickerReport();
        DQuant.getInstance().runUserPickAnalysis("HistoryTest 2023-01-01 2023-12-31",
            instancePickStrategy, 
            report);
        report.dump();

		CSystem.stop();
	}
}
