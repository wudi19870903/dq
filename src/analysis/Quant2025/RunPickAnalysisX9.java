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
public class RunPickAnalysisX9 implements IStockPickStrategy {
    
    /**
     * 判断今日是否符合中期创新高后的极速下跌形态
     * 
     * @param list K线列表
     * @param midTermDays 中期天数（用于判断创新高）
     * @param recentDropDays 近期下跌天数
     * @param dropPercent 下跌幅度阈值（百分比）
     * @param volumeIncreasePercent 成交量增加阈值（百分比）
     * @return 是否符合形态
     */
    public boolean isMidTermHighThenSharpDrop(
            CListObserver<KLine> list, 
            int midTermDays, 
            int recentDropDays, 
            double dropPercent,
            double volumeIncreasePercent) {
        
        int size = list.size();
        if (size < midTermDays + recentDropDays) {
            return false; // 数据不足
        }
        
        // 1. 检查中期内是否创出新高
        int highPointIndex = findMidTermHighPoint(list, midTermDays);
        if (highPointIndex == -1) {
            return false; // 没有找到中期高点
        }
        
        // 2. 检查从高点开始的快速下跌
        return checkSharpDropFromHigh(
            list, highPointIndex, recentDropDays, dropPercent, volumeIncreasePercent);
    }
    
    /**
     * 寻找中期内的最高点
     */
    private int findMidTermHighPoint(CListObserver<KLine> list, int midTermDays) {
        int size = list.size();
        int startIndex = Math.max(0, size - midTermDays);
        
        double highestHigh = Double.MIN_VALUE;
        int highestIndex = -1;
        
        // 寻找中期内的最高点
        for (int i = startIndex; i < size; i++) {
            KLine kline = list.get(i);
            if (kline.high > highestHigh) {
                highestHigh = kline.high;
                highestIndex = i;
            }
        }
        
        // 确保最高点不是今日（我们需要的是创新高后的下跌）
        if (highestIndex == size - 1) {
            return -1;
        }
        
        return highestIndex;
    }
    
    /**
     * 检查从高点开始的快速下跌
     */
    private boolean checkSharpDropFromHigh(
            CListObserver<KLine> list, 
            int highPointIndex, 
            int recentDropDays, 
            double dropPercent,
            double volumeIncreasePercent) {
        
        int size = list.size();
        KLine highPointKLine = list.get(highPointIndex);
        
        // 1. 检查下跌幅度
        KLine todayKLine = list.get(size - 1);
        double dropPercentage = (highPointKLine.high - todayKLine.close) / highPointKLine.high * 100;
        
        if (dropPercentage < dropPercent) {
            return false; // 跌幅不够
        }
        
        // 2. 检查下跌天数
        int actualDropDays = size - 1 - highPointIndex;
        if (actualDropDays > recentDropDays) {
            return false; // 下跌时间过长
        }
        
        // 3. 检查下跌过程中的成交量（可选）
        if (volumeIncreasePercent > 0) {
            double avgVolumeBeforeHigh = calculateAverageVolume(list, highPointIndex - 5, highPointIndex - 1);
            double avgVolumeDuringDrop = calculateAverageVolume(list, highPointIndex + 1, size - 1);
            
            if (avgVolumeDuringDrop < avgVolumeBeforeHigh * (1 + volumeIncreasePercent / 100)) {
                return false; // 下跌时成交量没有明显增加
            }
        }
        
        // 4. 检查下跌是否连续（大多数交易日下跌）
        int dropCount = 0;
        for (int i = highPointIndex + 1; i < size; i++) {
            if (list.get(i).close < list.get(i - 1).close) {
                dropCount++;
            }
        }
        
        double dropRatio = (double) dropCount / (size - highPointIndex - 1);
        return dropRatio >= 0.7; // 70%以上的交易日在下跌
    }
    
    /**
     * 计算指定区间的平均成交量
     */
    private double calculateAverageVolume(CListObserver<KLine> list, int startIndex, int endIndex) {
        if (startIndex < 0) startIndex = 0;
        if (endIndex >= list.size()) endIndex = list.size() - 1;
        if (startIndex > endIndex) return 0;
        
        double sum = 0;
        int count = 0;
        
        for (int i = startIndex; i <= endIndex; i++) {
            sum += list.get(i).volume;
            count++;
        }
        
        return count > 0 ? sum / count : 0;
    }

     @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isMidTermHighThenSharpDrop(
            kLines, 
            60,   // 中期为60天
            5,    // 近期下跌不超过5天
            15.0, // 下跌幅度至少15%
            20.0  // 下跌时成交量增加至少20%
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
