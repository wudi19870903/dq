package analysis.Quant2025;

import java.lang.invoke.MethodHandles;
import java.util.List;

import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAContext;
import pers.di.dquant.DQuant;
import pers.di.dquant.IStockPickStrategy;
import pers.di.dquant.internal.PickerReport;
import pers.di.model.KLine;

/**
 * 特征：
 * 长期横盘后，极速上涨后脱离横盘成本，回调一段时间
 */
public class RunPickAnalysisX4 implements IStockPickStrategy {

    public boolean isMatch(CListObserver<KLine> list) {
        // 参数设置
        int horizontalDays = 20; // 横盘天数
        double horizontalRangePercent = 0.05; // 横盘波动阈值（5%）
        double breakPercent = 0.10; // 突破涨幅（5%）
        double rapidRisePercent = 0.20; // 极速上涨涨幅（10%）
        int maxRiseDays = 5; // 极速上涨最大天数
        int minCallbackDays = 5; // 回调最小天数

        int n = list.size();
        // 检查数据是否足够
        if (n < horizontalDays + maxRiseDays + minCallbackDays) {
            return false;
        }

        // 从今日往前找突破日
        int breakDay = -1;
        for (int i = n - 1; i >= horizontalDays; i--) {
            // 计算i日之前horizontalDays天的最高价和最低价
            double maxInRange = Double.MIN_VALUE;
            double minInRange = Double.MAX_VALUE;
            for (int j = i - horizontalDays; j < i; j++) {
                KLine k = list.get(j);
                if (k.high > maxInRange) {
                    maxInRange = k.high;
                }
                if (k.low < minInRange) {
                    minInRange = k.low;
                }
            }
            // 检查横盘波动：价格范围是否在阈值内
            if ((maxInRange - minInRange) / minInRange <= horizontalRangePercent) {
                // 检查i日的收盘价是否突破横盘区间上轨
                KLine breakKLine = list.get(i);
                if (breakKLine.close > maxInRange * (1 + breakPercent)) {
                    breakDay = i;
                    break; // 找到最近的突破日
                }
            }
        }

        if (breakDay == -1) {
            return false; // 没有找到突破日
        }

        // 从突破日到今日，找到最高点（峰值日）
        double maxAfterBreak = Double.MIN_VALUE;
        int peakDay = breakDay;
        for (int i = breakDay; i < n; i++) {
            KLine k = list.get(i);
            if (k.close > maxAfterBreak) {
                maxAfterBreak = k.close;
                peakDay = i;
            }
        }

        // 检查极速上涨：从突破日到峰值日的涨幅和天数
        double rise = (list.get(peakDay).close - list.get(breakDay).close) / list.get(breakDay).close;
        if (rise < rapidRisePercent) {
            return false;
        }
        if (peakDay - breakDay > maxRiseDays) {
            return false;
        }

        // 检查回调：今日是否是回调状态
        if (peakDay == n - 1) {
            return false; // 今日就是峰值日，没有回调
        }
        KLine todayKLine = list.get(n - 1);
        if (todayKLine.close >= list.get(peakDay).close) {
            return false; // 今日收盘价不低于峰值，不是回调
        }

        // 检查回调天数是否足够
        int callbackDays = n - 1 - peakDay;
        if (callbackDays < minCallbackDays) {
            return false;
        }

        // 检查今日收盘价是否仍在横盘区间之上（脱离横盘成本）
        // 重新计算突破日之前的横盘区间最高价
        double maxInRange = Double.MIN_VALUE;
        for (int j = breakDay - horizontalDays; j < breakDay; j++) {
            KLine k = list.get(j);
            if (k.high > maxInRange) {
                maxInRange = k.high;
            }
        }
        if (todayKLine.close < maxInRange) {
            return false; // 跌回横盘区间，不符合脱离成本
        }

        return true;
    }

    @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isMatch(kLines);
    };

    public static void runPickAnalysis() {
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
        DQuant.getInstance().runUserPickAnalysis("HistoryTest 2023-01-01 2024-01-02",
            instancePickStrategy, 
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
