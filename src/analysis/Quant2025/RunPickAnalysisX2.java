package analysis.Quant2025;

import java.lang.invoke.MethodHandles;

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
 * 极涨后的大幅回调
 * 测试结果
 * HistoryTest 2020-01-01 2024-01-02
 * check 20 days, check win 0.050000, check lose 0.200000
 * short win rate: 0.508993 (283/556)
 * short lose rate: 0.097122 (54/556)
 * unknown rate: 0.393885 (219/556)
 */

public class RunPickAnalysisX2 implements IStockPickStrategy {

    
    public static boolean isContinuousCorrectionAfterRapidRiseNoPriorRise(CListObserver<KLine> list, 
                                                                         int riseDays, 
                                                                         double riseThreshold, 
                                                                         int correctionDays,
                                                                         double correctionThreshold,
                                                                         double volumeRatio,
                                                                         int priorCheckDays,
                                                                         double priorRiseThreshold) {
        // 检查输入有效性
        if (list == null || list.size() < riseDays + correctionDays + priorCheckDays + 1) {
            return false;
        }
        
        int todayIndex = list.size() - 1;
        
        // 1. 检查回调前是否有连续急涨
        int riseStartIndex = todayIndex - correctionDays - riseDays;
        if (riseStartIndex < 0) return false;
        
        // 计算基准期平均交易量（急涨开始前5天的平均交易量）
        double baseAvgVolume = 0;
        int baseCount = Math.min(5, riseStartIndex);
        for (int i = 1; i <= baseCount; i++) {
            baseAvgVolume += list.get(riseStartIndex - i).volume;
        }
        if (baseCount > 0) {
            baseAvgVolume /= baseCount;
        }
        
        // 检查急涨阶段
        for (int i = 0; i < riseDays; i++) {
            int currentIndex = riseStartIndex + i;
            int prevIndex = currentIndex - 1;
            
            if (prevIndex < 0) return false;
            
            KLine current = list.get(currentIndex);
            KLine prev = list.get(prevIndex);
            
            // 检查是否上涨（收盘价高于前一日收盘价）
            if (current.close <= prev.close) {
                return false;
            }
            
            // 检查涨幅是否达到阈值
            double riseRate = (current.close - prev.close) / prev.close;
            if (riseRate < riseThreshold) {
                return false;
            }
            
            // 检查交易量是否放大
            if (current.volume < baseAvgVolume * volumeRatio) {
                return false;
            }
        }
        
        // 2. 检查是否处于回调阶段（今日和之前correctionDays-1天）
        for (int i = 0; i < correctionDays; i++) {
            int currentIndex = todayIndex - i;
            int prevIndex = currentIndex - 1;
            
            if (prevIndex < 0) return false;
            
            KLine current = list.get(currentIndex);
            KLine prev = list.get(prevIndex);
            
            // 检查是否下跌（收盘价低于前一日收盘价）
            if (current.close >= prev.close) {
                return false;
            }
            
            // 检查跌幅是否达到阈值
            double declineRate = (prev.close - current.close) / prev.close;
            if (declineRate < correctionThreshold) {
                return false;
            }
        }
        
        // 3. 检查急涨前期是否有大涨（排除前期已经大涨的情况）
        int priorCheckStartIndex = riseStartIndex - priorCheckDays;
        if (priorCheckStartIndex < 0) priorCheckStartIndex = 0;
        
        // 计算急涨开始前一段时间的涨幅
        double priorRiseRate = (list.get(riseStartIndex).close - list.get(priorCheckStartIndex).close) / 
                               list.get(priorCheckStartIndex).close;
        
        // 如果前期涨幅超过阈值，则不符合条件
        if (priorRiseRate > priorRiseThreshold) {
            return false;
        }
        
        return true;
    }
    
    // 使用默认参数的便捷方法
    public static boolean isContinuousCorrectionAfterRapidRiseNoPriorRise(CListObserver<KLine> list) {
        // 默认参数：
        // 连续3天急涨(涨幅>3%)，连续2天大幅回调(跌幅>2%)，交易量放大1.5倍
        // 检查急涨前20天，前期涨幅不超过15%
        return isContinuousCorrectionAfterRapidRiseNoPriorRise(list, 3, 0.03, 2, 0.02, 1.5, 20, 0.15);
    }

    @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isContinuousCorrectionAfterRapidRiseNoPriorRise(kLines);
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
        PickerReport report = new PickerReport(20, 0.10,0.10);
        DQuant.getInstance().runUserPickAnalysis("HistoryTest 2023-01-01 2023-12-31",
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
        CLog.config_setTag("DQUANT", true);
		runPickAnalysis();
		CSystem.stop();
	}
}
