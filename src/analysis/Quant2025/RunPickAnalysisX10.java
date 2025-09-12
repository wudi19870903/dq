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
    
    /**
     * 判断今日是否符合长期高处远离60日均线急跌触碰到60日均线的形态
     * 
     * @param list K线列表
     * @param longTermDays 长期天数（用于判断长期高位）
     * @param awayPercent 远离60日均线的百分比阈值
     * @param sharpDropDays 急跌天数阈值
     * @param dropPercent 急跌幅度阈值
     * @param touchTolerance 触碰60日均线的容差百分比
     * @return 是否符合形态
     */
    public boolean isLongTermHighAwayThenSharpDropToMA60(
            CListObserver<KLine> list, 
            int longTermDays, 
            double awayPercent,
            int sharpDropDays, 
            double dropPercent,
            double touchTolerance) {
        
        int size = list.size();
        if (size < Math.max(60, longTermDays) + sharpDropDays) {
            return false; // 数据不足
        }
        
        // 1. 计算60日均线
        double[] ma60 = calculateMA(list, 60);
        if (ma60 == null) {
            return false; // 无法计算60日均线
        }
        
        // 2. 检查长期高位运行且远离60日均线
        int highAwayIndex = findHighAwayFromMA60(list, ma60, longTermDays, awayPercent);
        if (highAwayIndex == -1) {
            return false; // 没有找到符合条件的长期高位
        }
        
        // 3. 检查从高位开始的急跌
        boolean hasSharpDrop = checkSharpDrop(list, highAwayIndex, sharpDropDays, dropPercent);
        if (!hasSharpDrop) {
            return false; // 没有急跌
        }
        
        // 4. 检查今日是否触碰到60日均线
        return checkTouchMA60(list, ma60, touchTolerance);
    }
    
    /**
     * 计算移动平均线
     */
    private double[] calculateMA(CListObserver<KLine> list, int period) {
        int size = list.size();
        if (size < period) {
            return null;
        }
        
        double[] ma = new double[size];
        for (int i = 0; i < period - 1; i++) {
            ma[i] = 0; // 前period-1天无法计算MA
        }
        
        for (int i = period - 1; i < size; i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += list.get(j).close;
            }
            ma[i] = sum / period;
        }
        
        return ma;
    }
    
    /**
     * 寻找长期高位运行且远离60日均线的位置
     */
    private int findHighAwayFromMA60(
            CListObserver<KLine> list, 
            double[] ma60, 
            int longTermDays, 
            double awayPercent) {
        
        int size = list.size();
        int startIndex = Math.max(60, size - longTermDays);
        
        // 检查是否有足够的天数处于高位且远离60日均线
        int consecutiveDays = 0;
        int lastHighIndex = -1;
        
        for (int i = startIndex; i < size; i++) {
            KLine kline = list.get(i);
            double ma60Value = ma60[i];
            
            if (ma60Value == 0) {
                continue; // 跳过无效的MA值
            }
            
            // 计算价格相对于60日均线的偏离百分比
            double awayPercentage = (kline.close - ma60Value) / ma60Value * 100;
            
            if (awayPercentage >= awayPercent) {
                consecutiveDays++;
                lastHighIndex = i;
            } else {
                // 如果连续天数被打断，重置计数
                if (consecutiveDays < longTermDays / 3) { // 允许短暂回调
                    consecutiveDays = 0;
                }
            }
            
            // 如果找到足够长时间的远离
            if (consecutiveDays >= longTermDays / 2) { // 至少一半的时间处于高位
                return lastHighIndex;
            }
        }
        
        return -1; // 没有找到符合条件的长期高位
    }
    
    /**
     * 检查从高位开始的急跌
     */
    private boolean checkSharpDrop(
            CListObserver<KLine> list, 
            int startIndex, 
            int sharpDropDays, 
            double dropPercent) {
        
        int size = list.size();
        if (startIndex >= size - 1) {
            return false; // 起始位置无效
        }
        
        KLine startKLine = list.get(startIndex);
        KLine todayKLine = list.get(size - 1);
        
        // 计算从高位到今日的跌幅
        double dropPercentage = (startKLine.close - todayKLine.close) / startKLine.close * 100;
        
        if (dropPercentage < dropPercent) {
            return false; // 跌幅不够
        }
        
        // 检查下跌天数
        int actualDropDays = size - 1 - startIndex;
        if (actualDropDays > sharpDropDays) {
            return false; // 下跌时间过长，不是急跌
        }
        
        // 检查下跌是否连续（大多数交易日下跌）
        int dropCount = 0;
        for (int i = startIndex + 1; i < size; i++) {
            if (list.get(i).close < list.get(i - 1).close) {
                dropCount++;
            }
        }
        
        double dropRatio = (double) dropCount / (size - startIndex - 1);
        return dropRatio >= 0.7; // 70%以上的交易日在下跌
    }

    /**
     * 检查今日是否触碰到60日均线
     */
    private boolean checkTouchMA60(CListObserver<KLine> list, double[] ma60, double touchTolerance) {
        int size = list.size();
        KLine todayKLine = list.get(size - 1);
        double todayMA60 = ma60[size - 1];

        if (todayMA60 == 0) {
            return false; // 无效的MA值
        }

        // 计算今日收盘价与60日均线的接近程度
        double touchPercentage = Math.abs(todayKLine.close - todayMA60) / todayMA60 * 100;

        // 检查今日最低价是否低于60日均线（表示曾触碰或跌破）
        boolean touchedByLow = todayKLine.low <= todayMA60;

        // 检查收盘价是否接近60日均线
        boolean closedNear = touchPercentage <= touchTolerance;

        return touchedByLow || closedNear;
    }

     @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isLongTermHighAwayThenSharpDropToMA60(
    kLines, 
    30,   // 长期高位至少30天
    20.0, // 远离60日均线至少20%
    10,   // 急跌不超过10天
    15.0, // 急跌幅度至少15%
    2.0   // 触碰60日均线的容差2%
);
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
