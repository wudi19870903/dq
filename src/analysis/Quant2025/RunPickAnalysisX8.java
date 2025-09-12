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
 * 急跌横盘再次急跌形态检测
 * 下面是一个Java实现，用于判断今日是否符合"急跌横盘一段再次急跌"的形态。
 * 这个形态通常表示市场经历了快速下跌、整理阶段后再次快速下跌。
 */
public class RunPickAnalysisX8 implements IStockPickStrategy {
    
    /**
     * 判断今日是否符合急跌横盘再次急跌形态
     * 
     * @param list K线列表
     * @param sharpDropDays 急跌阶段的天数阈值
     * @param dropPercent 急跌的跌幅百分比阈值
     * @param consolidationDays 横盘阶段的天数阈值
     * @param consolidationRangePercent 横盘阶段的振幅百分比阈值
     * @param recentDropDays 最近急跌的天数阈值
     * @param recentDropPercent 最近急跌的跌幅百分比阈值
     * @return 是否符合形态
     */
    public boolean isSharpDropConsolidationDropPattern(
            CListObserver<KLine> list, 
            int sharpDropDays, 
            double dropPercent,
            int consolidationDays, 
            double consolidationRangePercent,
            int recentDropDays, 
            double recentDropPercent) {
        
        int size = list.size();
        if (size < sharpDropDays + consolidationDays + recentDropDays) {
            return false; // 数据不足
        }
        if (size < 250) {
            return false;
        }
        
        // 1. 寻找最初的急跌阶段
        int sharpDropEndIndex = findSharpDropEndIndex(list, sharpDropDays, dropPercent);
        if (sharpDropEndIndex == -1) {
            return false; // 没有找到急跌阶段
        }
        
        // 2. 检查横盘阶段
        int consolidationEndIndex = findConsolidationEndIndex(
            list, sharpDropEndIndex, consolidationDays, consolidationRangePercent);
        if (consolidationEndIndex == -1) {
            return false; // 没有找到横盘阶段
        }
        
        // 3. 检查最近的急跌阶段（包括今日）
        return checkRecentSharpDrop(list, consolidationEndIndex, recentDropDays, recentDropPercent);
    }
    
    /**
     * 寻找最初的急跌阶段结束位置
     */
    private int findSharpDropEndIndex(CListObserver<KLine> list, int sharpDropDays, double dropPercent) {
        int size = list.size();
        
        for (int i = sharpDropDays; i < size; i++) {
            double startPrice = list.get(i - sharpDropDays).close;
            double endPrice = list.get(i).close;
            double dropPercentage = (startPrice - endPrice) / startPrice * 100;
            
            if (dropPercentage >= dropPercent) {
                // 检查这个急跌阶段是否是连续下跌
                boolean isContinuousDrop = true;
                for (int j = i - sharpDropDays + 1; j <= i; j++) {
                    if (list.get(j).close > list.get(j - 1).close) {
                        isContinuousDrop = false;
                        break;
                    }
                }
                
                if (isContinuousDrop) {
                    return i; // 返回急跌阶段结束的索引
                }
            }
        }
        
        return -1; // 没有找到符合条件的急跌阶段
    }
    
    /**
     * 寻找横盘阶段结束位置
     */
    private int findConsolidationEndIndex(
            CListObserver<KLine> list, 
            int startIndex, 
            int consolidationDays, 
            double consolidationRangePercent) {
        
        int size = list.size();
        if (startIndex + consolidationDays >= size) {
            return -1; // 数据不足
        }
        
        for (int i = startIndex + 1; i <= size - consolidationDays; i++) {
            double highest = Double.MIN_VALUE;
            double lowest = Double.MAX_VALUE;
            
            // 计算横盘阶段内的最高价和最低价
            for (int j = i; j < i + consolidationDays; j++) {
                highest = Math.max(highest, list.get(j).high);
                lowest = Math.min(lowest, list.get(j).low);
            }
            
            double rangePercentage = (highest - lowest) / lowest * 100;
            
            if (rangePercentage <= consolidationRangePercent) {
                return i + consolidationDays - 1; // 返回横盘阶段结束的索引
            }
        }
        
        return -1; // 没有找到符合条件的横盘阶段
    }
    
    /**
     * 检查最近的急跌阶段
     */
    private boolean checkRecentSharpDrop(
            CListObserver<KLine> list, 
            int startIndex, 
            int recentDropDays, 
            double recentDropPercent) {
        
        int size = list.size();
        if (startIndex + recentDropDays >= size) {
            return false; // 数据不足
        }
        
        // 计算最近急跌阶段的跌幅
        double startPrice = list.get(startIndex + 1).close;
        double endPrice = list.get(size - 1).close; // 今日收盘价
        double dropPercentage = (startPrice - endPrice) / startPrice * 100;
        
        if (dropPercentage < recentDropPercent) {
            return false; // 跌幅不够
        }
        
        // 检查最近急跌阶段是否是连续下跌（允许小幅反弹）
        int dropCount = 0;
        for (int i = startIndex + 2; i < size; i++) {
            if (list.get(i).close < list.get(i - 1).close) {
                dropCount++;
            }
        }
        
        // 要求下跌天数占总天数的比例较高
        double dropRatio = (double) dropCount / (size - startIndex - 2);
        return dropRatio >= 0.7; // 70%以上的天数在下跌
    }

     @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isSharpDropConsolidationDropPattern(
            kLines, 
            5,   // 急跌阶段至少5天
            15.0, // 急跌跌幅至少15%
            10,  // 横盘阶段至少10天
            8.0, // 横盘振幅不超过8%
            3,   // 最近急跌至少3天
            10.0 // 最近急跌跌幅至少10%
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
