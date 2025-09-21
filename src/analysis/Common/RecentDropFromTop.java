package analysis.Common;

import pers.di.common.CListObserver;
import pers.di.common.CObjectContainer;
import pers.di.model.KLine;

public class RecentDropFromTop {
    /*
     * 判断是否是短期回调
     * 参数：minDropRatio 最小回调跌幅
     * 参数：maxDrapDays 收大回调天数
     * 参数：outTopIndex 返回高点索引
     * 
     * 关键特征：高点在近期，今天是高点后的最低点，高点前期有比今天还低的点
     * 
     * 典型：
     * 002354 2022-02-28
     */
    public static boolean isRecentDropFromTopLatest(CListObserver<KLine> list,
            double minDropRatio, int maxDrapDays, CObjectContainer<Integer> outTopIndex) {
        return isRecentDropFromTop(list, list.size() - 1, minDropRatio, maxDrapDays, outTopIndex);
    }
    public static boolean isRecentDropFromTop(CListObserver<KLine> list, int index,
            double minDropRatio, int maxDrapDays, CObjectContainer<Integer> outTopIndex) {
        // index过大，index过小，忽略
        if (index > list.size() - 1) {
            return false;
        }
        if (index < maxDrapDays + 20) {
            return false;
        }
        // 计算maxDrapDays+10天之内的最高点最低点
        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        int highIndex = 0;
        int lowIndex = 0;
        for (int i = index - maxDrapDays - 10; i <= index; i++) {
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
        // 最低点在top之前
        if (lowIndex < highIndex) {
        } else {
            return false;
        }
        // 当前是top后的最低点
        KLine kCurrent = list.get(index);
        for (int i = highIndex; i <= index; i++) { 
            KLine k = list.get(i);
            if (k.close < kCurrent.close) {
                return false;
            }
        }
        // 跌幅过小忽略
        if ((kCurrent.close - high)/high > -minDropRatio) {
            return false;
        }
        // 回调天数过大忽略
        if (index - highIndex >= maxDrapDays) {
            return false;
        }
        // 设置返回参数
        if (null != outTopIndex) {
            outTopIndex.set(highIndex);
        }
        return true;
    }
}
