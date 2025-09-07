package analysis.Quant2025;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;
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
 * 下跌后的早晨之星
 */
public class RunPickAnalysisX1 implements IStockPickStrategy {
    /**
     * 检查交易量模式
     * @param day1 第一天
     * @param day2 第二天
     * @param day3 第三天
     * @return 是否符合交易量模式
     */
    private static boolean checkVolumePattern(KLine day1, KLine day2, KLine day3) {
        // 第一天通常放量下跌
        if (day1.volume < day2.volume * 1.2) {
            // 第一天交易量应该明显大于第二天
            return false;
        }

        // 第二天交易量通常萎缩
        if (day2.volume > day1.volume * 0.8) {
            // 第二天交易量应该明显小于第一天
            return false;
        }

        // 第三天交易量通常放大
        if (day3.volume < day2.volume * 1.2) {
            // 第三天交易量应该明显大于第二天
            return false;
        }

        return true;
    }

     /**
     * 检查早晨之星价格模式
     * @param day1 第一天（大阴线）
     * @param day2 第二天（星线）
     * @param day3 第三天（大阳线）
     * @return 是否符合价格模式
     */
    private static boolean checkMorningStarPricePattern(KLine day1, KLine day2, KLine day3) {
        // 第一天是大阴线：收盘价远低于开盘价（跌幅大于4%）
        double day1Change = (day1.open - day1.close) / day1.open;
        if (day1Change <= 0.04) {
            return false;
        }

        // 第二天是星线：跳空低开，实体较小（振幅小于3%）
        if (day2.open >= day1.close * 0.99) { // 没有明显跳空低开
            return false;
        }

        double day2Amplitude = (day2.high - day2.low) / day2.open;
        if (day2Amplitude >= 0.03) { // 振幅太大，不是星线
            return false;
        }

        // 第三天是大阳线：收盘价远高于开盘价（涨幅大于3%），且收盘价至少回升到第一天实体的一半以上
        double day3Change = (day3.close - day3.open) / day3.open;
        if (day3Change <= 0.03) {
            return false;
        }

        double day1MidPoint = (day1.open + day1.close) / 2;
        if (day3.close < day1MidPoint) {
            return false;
        }

        return true;
    }

    /**
     * 检查中期急跌条件：过去20个交易日下跌超过15%
     * @param list K线列表
     * @param startIndex 起始索引
     * @param endIndex 结束索引
     * @return 是否符合急跌条件
     */
    private static boolean checkMidTermSharpDecline(CListObserver<KLine> list, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex >= list.size() || startIndex >= endIndex) {
            return false;
        }

        double startPrice = list.get(startIndex).close;
        double endPrice = list.get(endIndex).close;
        double declineRatio = (startPrice - endPrice) / startPrice;

        // 下跌超过15%
        return declineRatio >= 0.15;
    }

     /**
     * 判断今日是否符合中期急跌后的早晨之星模式
     * @param list 日K线列表，按时间顺序排列（最早的数据在索引0，今日的数据在最后）
     * @return 是否符合早晨之星模式
     */
    public static boolean isMorningStarAfterSharpDecline(CListObserver<KLine> list) {
        int size = list.size();

        // 需要至少21根K线来判断中期趋势和最近3天的模式
        if (size < 21) {
            return false;
        }

        // 获取最近三天的K线
        KLine dayBeforeYesterday = list.get(size - 3); // 前天
        KLine yesterday = list.get(size - 2);          // 昨天
        KLine today = list.get(size - 1);              // 今天

        // 检查中期急跌条件：过去20个交易日下跌超过15%
        if (!checkMidTermSharpDecline(list, size - 21, size - 1)) {
            return false;
        }

        // 检查早晨之星价格模式
        if (!checkMorningStarPricePattern(dayBeforeYesterday, yesterday, today)) {
            return false;
        }

        // 检查交易量模式
        // if (!checkVolumePattern(dayBeforeYesterday, yesterday, today)) {
        //     return false;
        // }

        return true;
    }

    @Override
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return isMorningStarAfterSharpDecline(kLines);
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
