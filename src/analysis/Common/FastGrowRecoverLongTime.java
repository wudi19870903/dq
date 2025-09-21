package analysis.Common;

import pers.di.common.CListObserver;
import pers.di.model.KLine;

public class FastGrowRecoverLongTime {
    /*
     * 判断是否是快速拉涨，收复过一段时间
     * 参数：minFastGrowRatio 最小涨幅
     * 参数：longTimeDays 收复最小天数
     * 
     * 关键特征：近期急速拉高，拉高收复最近一段时间最高点，最低点在近期
     * 
     * 典型：
     * 002468 2024-02-07
     * 002354 2022-02-24
     */
    public static boolean checkLatest(CListObserver<KLine> list,
        double minFastGrowRatio, int minRecoveryDays) {
        return check(list, list.size() - 1, minFastGrowRatio, minRecoveryDays);
    }
    public static boolean check(CListObserver<KLine> list, int index,
        double minFastGrowRatio, int minRecoveryDays) {
        // index过大，index过小，忽略
        if (index > list.size() - 1) {
            return false;
        }
        if (index < minRecoveryDays + 10) {
            return false;
        }
        // 计算最近三天的涨幅
        double fastGrowHeight = Double.MIN_VALUE;
        double fastGrowLow = Double.MAX_VALUE;
        int fastGrowHightIndex = 0;
        int fastGrowLowIndex = 0;
        for (int i = index - 2; i <= index; i++) {
            KLine k = list.get(i);
            if (k.close > fastGrowHeight) {
                fastGrowHeight = k.high;
                fastGrowHightIndex = i;
            }
            if (k.close < fastGrowLow) {
                fastGrowLow = k.close;
                fastGrowLowIndex = i;
            }
        }
        // 不符合快速涨幅忽略
        if ((fastGrowHeight - fastGrowLow)/fastGrowLow < minFastGrowRatio
            || fastGrowLowIndex > fastGrowHightIndex) {
            return false;
        }
        // 最高点不是今天忽略
        if (fastGrowHightIndex != index) {
            return false;
        }
        // 从最高点往前循环判定最低点与最高点位置
        KLine kLow = null;
        double low = Double.MAX_VALUE;
        int lowIndex = -1;
        KLine kHigh= null;
        double high = Double.MIN_VALUE;
        int highIndex = -1;
        for (int i = fastGrowHightIndex - 1; i > 0; i--) {
            KLine k = list.get(i);
            if (k.close < low) {
                kLow = k;
                low = k.close;
                lowIndex = i;
            }
            if (k.close > high) {
                kHigh = k;
                high = k.close;
                highIndex = i;
            }
            // 遍历到最高点大于拉涨点截止
            if (high > fastGrowHeight) {
                //CLog.info("TEST", "high date:%s low:%s", kHigh.date, kLow.date);
                // 最低点离快速拉涨太远，忽略
                if (fastGrowHightIndex - lowIndex > 5) {
                    return false;
                }
                // 收复天数天数小于最小收复天数，忽略
                if (fastGrowHightIndex - highIndex < minRecoveryDays) {
                    return false;
                }

                // 符合快速拉涨收复
                return true;
            }
        }

        return false;
    }
}
