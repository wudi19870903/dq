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
 * 平台整理后的二次突破
 * 测试结果
 * HistoryTest 2020-01-01 2024-01-02
 * check 20 days, check win 0.050000, check lose 0.200000
 * short win rate: 0.459799 (366/796)
 * short lose rate: 0.020101 (16/796)
 * unknown rate: 0.520101 (414/796)
 */
public class RunPickAnalysisX3 implements IStockPickStrategy {

    public static boolean isPlatformBreakoutRetrySuccess(CListObserver<KLine> list, 
                                                        int platformDays, 
                                                        double platformRange, 
                                                        double breakoutThreshold,
                                                        double volumeRatio,
                                                        double retracementThreshold) {
        // 检查输入有效性
        if (list == null || list.size() < platformDays + 5) {
            return false;
        }
        
        int todayIndex = list.size() - 1;
        KLine today = list.get(todayIndex);
        
        // 1. 识别平台期
        int platformEndIndex = todayIndex - 2; // 假设平台期结束于前天
        int platformStartIndex = platformEndIndex - platformDays + 1;
        
        if (platformStartIndex < 0) {
            return false;
        }
        
        // 计算平台期的最高价和最低价
        double platformHigh = list.get(platformStartIndex).high;
        double platformLow = list.get(platformStartIndex).low;
        double platformVolumeSum = list.get(platformStartIndex).volume;
        
        for (int i = platformStartIndex + 1; i <= platformEndIndex; i++) {
            KLine kline = list.get(i);
            platformHigh = Math.max(platformHigh, kline.high);
            platformLow = Math.min(platformLow, kline.low);
            platformVolumeSum += kline.volume;
        }
        
        double platformAvgVolume = platformVolumeSum / platformDays;
        
        // 检查平台期振幅是否在合理范围内
        double platformRangeActual = (platformHigh - platformLow) / platformLow;
        if (platformRangeActual > platformRange) {
            return false; // 平台期振幅过大
        }
        
        // 2. 检查第一次突破尝试（昨天）
        KLine firstBreakoutDay = list.get(todayIndex - 1);
        
        // 检查是否突破了平台高点
        if (firstBreakoutDay.high <= platformHigh) {
            return false;
        }
        
        // 检查突破幅度是否达到阈值
        double firstBreakoutRate = (firstBreakoutDay.high - platformHigh) / platformHigh;
        if (firstBreakoutRate < breakoutThreshold) {
            return false;
        }
        
        // 检查成交量是否放大
        if (firstBreakoutDay.volume < platformAvgVolume * volumeRatio) {
            return false;
        }
        
        // 3. 检查第一次突破后的回调（今天开盘或盘中）
        // 检查今日是否有回调至平台区域内的迹象
        boolean hasRetracement = false;
        
        // 检查今日最低价是否回到平台区域内或接近平台高点
        if (today.low <= platformHigh * (1 + retracementThreshold) && 
            today.low >= platformLow * (1 - retracementThreshold)) {
            hasRetracement = true;
        }
        
        // 或者检查今日开盘价是否在平台区域内
        if (!hasRetracement && 
            today.open <= platformHigh * (1 + retracementThreshold) && 
            today.open >= platformLow * (1 - retracementThreshold)) {
            hasRetracement = true;
        }
        
        if (!hasRetracement) {
            return false;
        }
        
        // 4. 检查今日是否成功突破（收盘价高于平台高点）
        if (today.close <= platformHigh) {
            return false;
        }
        
        // 检查突破幅度是否显著
        double finalBreakoutRate = (today.close - platformHigh) / platformHigh;
        if (finalBreakoutRate < breakoutThreshold) {
            return false;
        }
        
        // 检查成交量是否继续放大或至少维持高位
        if (today.volume < platformAvgVolume * volumeRatio * 0.8) {
            return false; // 成交量不能显著萎缩
        }
        
        return true;
    }
    
    // 使用默认参数的便捷方法
    public static boolean isPlatformBreakoutRetrySuccess(CListObserver<KLine> list) {
        // 默认参数：
        // 平台期10天，振幅不超过5%，突破阈值2%，成交量放大1.5倍，回调阈值1%
        return isPlatformBreakoutRetrySuccess(list, 10, 0.05, 0.02, 1.5, 0.01);
    }


    @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isPlatformBreakoutRetrySuccess(kLines);
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
        DQuant.getInstance().runUserPickAnalysis("HistoryTest 2020-01-01 2024-01-02",
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
