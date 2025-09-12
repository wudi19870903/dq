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

public class RunPickAnalysisX6 implements IStockPickStrategy {
    public boolean isLongTermDeclineThenBigYang(CListObserver<KLine> list, int longTermDays, int[] maDays, double minIncreasePercent, int stableDays) {
        int size = list.size();
        // 检查列表大小是否足够
        int maxMaDay = 0;
        for (int maDay : maDays) {
            if (maDay > maxMaDay) {
                maxMaDay = maDay;
            }
        }
        int requiredSize = Math.max(longTermDays, maxMaDay) + stableDays;
        if (size < requiredSize) {
            //throw new IllegalArgumentException("List size is too small. Required: " + requiredSize);
            return false;
        }

        // 获取昨日索引
        int yesterdayIndex = size - 2;
        KLine today = list.get(size - 1);
        KLine yesterday = list.get(yesterdayIndex);

        // 1. 判断长期下跌：longTermDays天前的收盘价高于昨日收盘价
        int longTermStartIndex = size - 1 - longTermDays;
        if (longTermStartIndex < 0) {
            return false;
        }
        KLine longTermAgo = list.get(longTermStartIndex);
        if (yesterday.close >= longTermAgo.close) {
            return false; // 没有长期下跌
        }

        // 2. 判断止稳：最近stableDays天内没有创新低，且波动减小
        // 找出长期下跌期间的最低点（从longTermStartIndex到yesterdayIndex）
        double longTermLow = Long.MAX_VALUE;
        for (int i = longTermStartIndex; i <= yesterdayIndex; i++) {
            if (list.get(i).low < longTermLow) {
                longTermLow = list.get(i).low;
            }
        }
        // 检查最近stableDays天的最低点是否高于长期最低点（没有创新低）
        int stableStartIndex = size - stableDays - 1; // 从昨天往前推stableDays天
        if (stableStartIndex < 0) {
            return false;
        }
        double recentLow = Long.MAX_VALUE;
        for (int i = stableStartIndex; i <= yesterdayIndex; i++) {
            if (list.get(i).low < recentLow) {
                recentLow = list.get(i).low;
            }
        }
        if (recentLow <= longTermLow) {
            return false; // 创新低，未止稳
        }

        // 计算波动减小：比较最近stableDays天的价格范围与之前stableDays天的范围
        // 之前stableDays天：从longTermStartIndex到longTermStartIndex + stableDays - 1
        double previousRange = calculatePriceRange(list, longTermStartIndex, longTermStartIndex + stableDays - 1);
        double recentRange = calculatePriceRange(list, stableStartIndex, yesterdayIndex);
        // 如果最近范围大于之前范围，可能未止稳；这里要求最近范围小于之前范围的1.2倍（可调整）
        if (recentRange > previousRange * 1.2) {
            return false;
        }

        // 3. 计算均线（截至昨日）
        double[] maValues = new double[maDays.length];
        for (int i = 0; i < maDays.length; i++) {
            int n = maDays[i];
            double sum = 0;
            for (int j = yesterdayIndex; j > yesterdayIndex - n; j--) {
                sum += list.get(j).close;
            }
            maValues[i] = sum / n;
        }

        // 4. 检查大阳线：今日涨幅超过minIncreasePercent
        double increase = (today.close - today.open) / today.open * 100;
        if (increase < minIncreasePercent) {
            return false;
        }

        // 5. 检查穿过多日均线：今日开盘价低于所有均线，收盘价高于所有均线
        for (double maValue : maValues) {
            if (today.open >= maValue || today.close <= maValue) {
                return false;
            }
        }

        return true;
    }

    private double calculatePriceRange(CListObserver<KLine> list, int start, int end) {
        if (start < 0) start = 0;
        if (end >= list.size()) end = list.size() - 1;
        double min = Long.MAX_VALUE;
        double max = Long.MIN_VALUE;
        for (int i = start; i <= end; i++) {
            KLine k = list.get(i);
            if (k.low < min) min = k.low;
            if (k.high > max) max = k.high;
        }
        return max - min;
    }

     @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isLongTermDeclineThenBigYang(kLines, 30, new int[]{5, 10, 20}, 5.0, 5);
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
