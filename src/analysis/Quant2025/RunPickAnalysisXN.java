package analysis.Quant2025;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import analysis.Common.FastGrowRecoverLongTime;
import analysis.Common.GoldCrossChecker;
import analysis.Common.RecentDropFromTop;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAContext;
import pers.di.dataprovider.DataTestHelper;
import pers.di.dquant.DQuant;
import pers.di.dquant.IStockPickStrategy;
import pers.di.dquant.internal.PickerReport;
import pers.di.model.KLine;

/*
 * 
 */
public class RunPickAnalysisXN implements IStockPickStrategy {

    /*
     * 判断是否是跌稳回升
     * 参数：minDropRatio 最小跌幅（从高点下来最小跌幅）
     * 参数：maxDropDays 最大跌幅天数（从高点下来最大跌幅天数）
     * 参数：minStableDays 最小止跌天数
     * 参数：maxStableDays 最大止跌天数
     * 参数：maxRiseRatio 最大企稳涨幅（持续minStableDays天不创新低后的最大涨幅）
     * 
     * 关键特征：在较短实践内从高点跌下来，创造低点后，若干天不创造新低
     * 
     * 典型：
     * 301088 2022-04-29
     */
    public static boolean isFallStableAndRise(CListObserver<KLine> list, int index,
            double minDropRatio, int maxDropDays,
            int minStableDays, int maxStableDays, double maxRiseRatio,
            CObjectContainer<Integer> outLowIndex) {
        // index过大，index过小，忽略
        if (index > list.size() - 1) {
            return false;
        }
        if (index < maxDropDays + minStableDays + 20) {
            return false;
        }
        // 计算maxDropDays + minStableDays天之内的最高点最低点
        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        int highIndex = 0;
        int lowIndex = 0;
        for (int i = index - maxDropDays - minStableDays; i <= index; i++) {
            KLine k = list.get(i);
            if (k.close > high) {
                high = k.close;
                highIndex = i;
            }
            if (k.close < low) {
                low = k.close;
                lowIndex = i;
            }
        }
        KLine kHigh = list.get(highIndex);
        KLine kLow = list.get(lowIndex);
        KLine kCurrent = list.get(index);
        // 跌幅天数过长忽略
        if (highIndex >= lowIndex) {
            return false;
        }
        if (lowIndex - highIndex > maxDropDays) {
            return false;
        }
        // 跌幅过小忽略
        if ((high - low)/high < minDropRatio) {
            return false;
        }
        // 当前日离最低点过近或过远，忽略
        if (index - lowIndex < minStableDays || index - lowIndex > maxStableDays) {
            return false;
        }
        // 计算企稳阶段的涨幅， 涨幅过大忽略
        double riseRatio = (kCurrent.close - kLow.close)/kLow.close;
        if (riseRatio > maxRiseRatio) {
            return false;
        }
        // 设置返回参数
        if (null != outLowIndex) {
            outLowIndex.set(lowIndex);
        }
        return true;
    }

     @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        //CLog.info("TEST", "date:%s stockid:%s", context.date(), stockID);
        // if (!stockID.equals("002468") || !context.date().equals("2024-02-07")) {
        //     return false;
        // }
        // if (!stockID.equals("002354") || !context.date().contains("2022-02-2")) {
        //     return false;
        // }
        // if (!stockID.equals("600056") || !context.date().contains("2024-01-26")) {
        //     return false;
        // }
        // if (!stockID.equals("301088") || !context.date().contains("2022-04-29")) {
        //     return false;
        // }

        // 判断当前跌稳回升
        CObjectContainer<Integer> lowIndex = new CObjectContainer<>();
        boolean isFallStableAndRise = isFallStableAndRise(kLines, kLines.size() - 1,
            0.10, 5, 2, 10, 0.08, lowIndex);
        if (!isFallStableAndRise) {
            return false;
        }
        // 判断最低点属于短期回调
        int iLowIndex = lowIndex.get();
        CObjectContainer<Integer> topIndex = new CObjectContainer<>();
        boolean isDrop = RecentDropFromTop.isRecentDropFromTop(kLines, iLowIndex, 0.10, 5, topIndex);
        if (!isDrop) {
            return false;
        }
        // 判断高点属于快速拉涨突破高点
        boolean bFastGrowRecoverLongTime = FastGrowRecoverLongTime.check(kLines, topIndex.get(), 0.20, 60);
        if (!bFastGrowRecoverLongTime) {
            return false;
        }

        return true;
    };

    public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CLog.config_setTag("TEST", true);
        CLog.config_setTag("REPORT", true);
		CLog.config_setTag("ACCOUNT", false);
        CLog.config_setTag("DQUANT", true);

        // 可以使用测试数据减少测试量
		// DataTestHelper.InitLocalData(
        //     "2025-01-02",
        //     new ArrayList<>(Arrays.asList("999999", "002468", "002354", "301088")));
		
        // 动态创建本类的选股策略实例
        IStockPickStrategy instancePickStrategy = null;
        Class<?> clazz = MethodHandles.lookup().lookupClass();
        try {
            instancePickStrategy = (IStockPickStrategy) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 运行选股策略,输出选股结果
        PickerReport report = new PickerReport(20, 0.10, 0.10);
        DQuant.getInstance().runUserPickAnalysis("HistoryTest 2020-01-01 2023-12-31",
            instancePickStrategy, 
            report);
        report.dump();

		CSystem.stop();
	}
}
