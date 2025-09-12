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
 * 为了判断今日是否是长期下跌止稳后一根大阳线穿过多日均线后的回调日，
 * 我们需要先检查昨日是否满足“长期下跌止稳后一根大阳线穿过多日均线”的条件，
 * 然后检查今日是否回调（今日收盘价低于昨日收盘价）。以下是完整的Java代码实现：
 */
public class RunPickAnalysisX7 implements IStockPickStrategy {
    public boolean isPullbackAfterBigYang(CListObserver<KLine> list, int longTermDays, int[] maDays, double minIncreasePercent, int stableDays) {
        int size = list.size();
        if (size < 2) {
            return false; // 至少需要两天数据
        }
        int yesterdayIndex = size - 2;
        // 检查昨日是否满足大阳线穿过多日均线的条件
        boolean yesterdayIsBigYang = isBigYangCrossMA(list, yesterdayIndex, longTermDays, maDays, minIncreasePercent, stableDays);
        if (!yesterdayIsBigYang) {
            return false;
        }
        // 检查今日回调：今日收盘价低于昨日收盘价
        KLine yesterday = list.get(yesterdayIndex);
        KLine today = list.get(size - 1);
        return today.close < yesterday.close;
    }

    private boolean isBigYangCrossMA(CListObserver<KLine> list, int index, int longTermDays, int[] maDays, double minIncreasePercent, int stableDays) {
        int size = list.size();
        if (index <= 0 || index >= size) {
            return false; // 索引无效
        }

        // 计算最大均线天数
        int maxMaDay = 0;
        for (int maDay : maDays) {
            if (maDay > maxMaDay) {
                maxMaDay = maDay;
            }
        }
        // 检查数据是否足够
        int requiredSize = Math.max(longTermDays, maxMaDay) + stableDays;
        if (index < requiredSize) {
            return false; // 数据不足
        }

        KLine targetDay = list.get(index); // 目标日（大阳线日）
        KLine previousDay = list.get(index - 1); // 目标日的前一天

        // 1. 判断长期下跌：从index-longTermDays到index-1，收盘价下跌
        int longTermStartIndex = index - longTermDays;
        if (longTermStartIndex < 0) {
            return false;
        }
        KLine longTermAgo = list.get(longTermStartIndex);
        if (previousDay.close >= longTermAgo.close) {
            return false; // 没有长期下跌
        }

        // 2. 判断止稳：最近stableDays天（从index-stableDays到index-1）没有创新低，且波动减小
        // 找出长期下跌期间的最低点（从longTermStartIndex到index-1）
        double longTermLow = Double.MAX_VALUE;
        for (int i = longTermStartIndex; i < index; i++) {
            KLine k = list.get(i);
            if (k.low < longTermLow) {
                longTermLow = k.low;
            }
        }
        // 检查最近stableDays天（从index-stableDays到index-1）的最低点是否高于长期最低点
        int stableStartIndex = index - stableDays;
        if (stableStartIndex < 0) {
            return false;
        }
        double recentLow = Double.MAX_VALUE;
        for (int i = stableStartIndex; i < index; i++) {
            KLine k = list.get(i);
            if (k.low < recentLow) {
                recentLow = k.low;
            }
        }
        if (recentLow <= longTermLow) {
            return false; // 创新低，未止稳
        }

        // 计算波动减小：比较最近stableDays天的价格范围与之前stableDays天的范围
        double previousRange = calculatePriceRange(list, longTermStartIndex, longTermStartIndex + stableDays - 1);
        double recentRange = calculatePriceRange(list, stableStartIndex, index - 1);
        // 如果最近范围大于之前范围的1.2倍，则认为未止稳
        if (recentRange > previousRange * 1.2) {
            return false;
        }

        // 3. 计算均线（截至index-1天）
        double[] maValues = new double[maDays.length];
        for (int i = 0; i < maDays.length; i++) {
            int n = maDays[i];
            double sum = 0;
            int start = index - 1;
            int end = start - n + 1;
            if (end < 0) {
                return false; // 数据不足计算均线
            }
            for (int j = start; j >= end; j--) {
                sum += list.get(j).close;
            }
            maValues[i] = sum / n;
        }

        // 4. 检查大阳线：目标日的涨幅超过minIncreasePercent
        double increase = (targetDay.close - targetDay.open) / targetDay.open * 100;
        if (increase < minIncreasePercent) {
            return false;
        }

        // 5. 检查穿过多日均线：目标日开盘价低于所有均线，收盘价高于所有均线
        for (double maValue : maValues) {
            if (targetDay.open >= maValue || targetDay.close <= maValue) {
                return false;
            }
        }

        return true;
    }

    private double calculatePriceRange(CListObserver<KLine> list, int start, int end) {
        if (start < 0) start = 0;
        if (end >= list.size()) end = list.size() - 1;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = start; i <= end; i++) {
            KLine k = list.get(i);
            if (k.low < min) min = k.low;
            if (k.high > max) max = k.high;
        }
        return max - min;
    }

     @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isPullbackAfterBigYang(kLines, 30, new int[]{5, 10, 20}, 5.0, 5);
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
