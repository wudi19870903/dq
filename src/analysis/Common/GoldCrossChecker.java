package analysis.Common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pers.di.common.CListObserver;
import pers.di.model.KLine;

public class GoldCrossChecker {
    // 计算EMA指标
    private static List<Double> calculateEMA(List<Double> prices, int period) {
        int size = prices.size();
        List<Double> ema = new ArrayList<>(Collections.nCopies(size, Double.NaN));
        if (size < period) {
            return ema;
        }
        double alpha = 2.0 / (period + 1);
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += prices.get(i);
        }
        ema.set(period - 1, sum / period);
        for (int i = period; i < size; i++) {
            double emaToday = alpha * prices.get(i) + (1 - alpha) * ema.get(i - 1);
            ema.set(i, emaToday);
        }
        return ema;
    }

    // 判断今日是否是长期未出现MACD金叉后的首次金叉
    public static boolean isFirstGoldCrossAfterLongTime(CListObserver<KLine> list, int longTimeDays) {
        int size = list.size();
        if (size < 34) {
            return false; // 数据不足，无法计算MACD
        }

        // 提取收盘价列表
        List<Double> closes = new ArrayList<>();
        for (KLine k : list) {
            closes.add(k.close);
        }

        // 计算12日EMA和26日EMA
        List<Double> ema12 = calculateEMA(closes, 12);
        List<Double> ema26 = calculateEMA(closes, 26);

        // 计算DIF（差离值），从索引25（第26天）开始
        List<Double> dif = new ArrayList<>(Collections.nCopies(size, Double.NaN));
        for (int i = 25; i < size; i++) {
            dif.set(i, ema12.get(i) - ema26.get(i));
        }

        // 计算DEA（信号线），从索引33（第34天）开始
        List<Double> dea = new ArrayList<>(Collections.nCopies(size, Double.NaN));
        if (size > 33) {
            double sum = 0;
            for (int i = 25; i <= 33; i++) {
                sum += dif.get(i);
            }
            dea.set(33, sum / 9); // 初始DEA为9日SMA of DIF
            double alpha = 2.0 / 10; // alpha = 2/(9+1)
            for (int i = 34; i < size; i++) {
                dea.set(i, alpha * dif.get(i) + (1 - alpha) * dea.get(i - 1));
            }
        }

        // 查找历史上最后一次金叉（今日之前）
        int lastGoldCrossIndex = -1;
        for (int i = 34; i < size - 1; i++) { // 从索引34到倒数第二天
            if (!Double.isNaN(dif.get(i)) && !Double.isNaN(dea.get(i)) &&
                !Double.isNaN(dif.get(i - 1)) && !Double.isNaN(dea.get(i - 1))) {
                if (dif.get(i) > dea.get(i) && dif.get(i - 1) <= dea.get(i - 1)) {
                    lastGoldCrossIndex = i;
                }
            }
        }

        // 检查今日是否出现金叉
        boolean todayGoldCross = false;
        int todayIndex = size - 1;
        if (todayIndex >= 34) {
            if (!Double.isNaN(dif.get(todayIndex)) && !Double.isNaN(dea.get(todayIndex)) &&
                !Double.isNaN(dif.get(todayIndex - 1)) && !Double.isNaN(dea.get(todayIndex - 1))) {
                todayGoldCross = dif.get(todayIndex) > dea.get(todayIndex) && 
                                 dif.get(todayIndex - 1) <= dea.get(todayIndex - 1);
            }
        }

        if (!todayGoldCross) {
            return false;
        }

        // 如果历史上没有金叉，今日金叉即为首次
        if (lastGoldCrossIndex == -1) {
            return true;
        }

        // 计算自从上次金叉以来的天数
        int daysSinceLastGoldCross = todayIndex - lastGoldCrossIndex;
        return daysSinceLastGoldCross >= longTimeDays;
    }
}
