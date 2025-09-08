package pers.di.dquant.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.dataengine.DAContext;
import pers.di.model.KLine;
import pers.di.model.StockUtils;

/*
 * 选股报告
 * 在检查周期内，测量触发盈利和亏损的情况
 * 默认值是检查20交易日内，盈利10%与亏损10%的情况
 */
public class PickerReport {
    public PickerReport () {
        this(20, 0.10, 0.10);
    }

    public PickerReport (long dayCountCheck, double winRateCheck, double loseRateCheck) {
        DAYCOUNT_CHECK = dayCountCheck;
        WIN_RATE_CHECK = winRateCheck;
        LOSE_RATE_CHECK = loseRateCheck;
        pickList = new ArrayList<Entry<String, String>>();
        pickKLineMap = new java.util.HashMap<Entry<String, String>, KLine>();
        shortWinMap = new java.util.HashMap<Entry<String, String>, String>();
        shortWinKLineMap = new java.util.HashMap<Entry<String, String>, KLine>();
        shortLoseMap = new java.util.HashMap<Entry<String, String>, String>();
        shortLoseKLineMap = new java.util.HashMap<Entry<String, String>, KLine>();
        shortTimeoutMap = new java.util.HashMap<Entry<String, String>, String>();
        shortTimeoutKLineMap = new java.util.HashMap<Entry<String, String>, KLine>();
        shortTimeoutWinRateMap = new java.util.HashMap<Entry<String, String>, Double>();
    }

    public void onTradingDayFinish(DAContext context) {
        // 统计选入列表的后期表现
        // 如：10天内，出现亏损5%，出现盈利5%，进行统计
        for (int i = 0; i < this.pickList.size(); i++) {
            Entry<String, String> pair = this.pickList.get(i);
            String datePick = pair.getKey();
            String stockID = pair.getValue();
            CListObserver<KLine> klineList = context.getDayKLinesBackwardAdjusted(stockID);
            if (null == klineList) {
                continue;
            }
            KLine kline = klineList.end();
            if (StockUtils.getDayCountBetweenBeginEnd(klineList, datePick, kline.date) < DAYCOUNT_CHECK) {
                // 在检查期限内，已经确认有盈利或者亏损阈值触发，忽略这个时间段内的pick
                if (this.shortLoseMap.containsKey(pair)) {
                    continue;
                }
                if (this.shortWinMap.containsKey(pair)) {
                    continue;
                }
                KLine klinePick = this.pickKLineMap.get(pair);
                double winRate = (kline.close - klinePick.close)/klinePick.close;
                if (winRate <= -LOSE_RATE_CHECK) {
                    this.shortLoseMap.put(pair, kline.date);
                    this.shortLoseKLineMap.put(pair, kline);
                    CLog.info("DQUANT", "StockPickStrategy date:%s stockID:%s shortLose %s", 
                        pair.getKey(), pair.getValue(), kline.date);
                } else if (winRate >= WIN_RATE_CHECK) {
                    this.shortWinMap.put(pair, kline.date);
                    this.shortWinKLineMap.put(pair, kline);
                    CLog.info("DQUANT", "StockPickStrategy date:%s stockID:%s shortWin %s", 
                        pair.getKey(), pair.getValue(), kline.date);
                } else {
                    // do nothing
                }
            } else {
                // 超时的pick处理
                // 在检查期限内，已经确认有盈利或者亏损阈值触发，忽略这个时间段内的pick
                if (this.shortLoseMap.containsKey(pair)) {
                    continue;
                }
                if (this.shortWinMap.containsKey(pair)) {
                    continue;
                }
                if (this.shortTimeoutMap.containsKey(pair)) {
                    continue;
                }
                KLine klinePick = this.pickKLineMap.get(pair);
                double winRate = (kline.close - klinePick.close)/klinePick.close;
                this.shortTimeoutMap.put(pair, kline.date);
                this.shortTimeoutKLineMap.put(pair, kline);
                this.shortTimeoutWinRateMap.put(pair, winRate);
            }
        }
    }

    public void reset() {
        pickList.clear();
    }

    public void dump() { 
        CLog.info("REPORT", "-------- PickerReport dump --------");
        CLog.info("REPORT", "pick list:");
        for (Entry<String, String> pair : pickList) {
            KLine kline = pickKLineMap.get(pair);
            CLog.info("REPORT", "    pick: %s, %s, %.2f", pair.getKey(), pair.getValue(), kline.close);
        }
        CLog.info("REPORT", "short win list:");
        for (Entry<String, String> pair : shortWinMap.keySet()) {
            CLog.info("REPORT", "    short win: %s, %s, %s", pair.getKey(), pair.getValue(), shortWinMap.get(pair));
        }
        CLog.info("REPORT", "short lose list:");
        for (Entry<String, String> pair : shortLoseMap.keySet()) {
            CLog.info("REPORT", "    short lose: %s, %s, %s", pair.getKey(), pair.getValue(), shortLoseMap.get(pair));
        }
        // timeout
        CLog.info("REPORT", "timeout list:");
        long cntWin_20plus = 0;
        long cntWin_15_20 = 0;
        long cntWin_10_15 = 0;
        long cntWin_5_10 = 0;
        long cntWin_0_5 = 0;
        long cntLose_0_5 = 0;
        long cntLose_5_10 = 0;
        long cntLose_10_15 = 0; 
        long cntLose_15_20 = 0; 
        long cntLose_20plus = 0;
        for (Entry<String, String> pair : shortTimeoutMap.keySet()) {
            Double winRate = shortTimeoutWinRateMap.get(pair);
            if (winRate >= 0.20) {
                cntWin_20plus++;
            } else if (winRate >= 0.15 && winRate < 0.20) {
                cntWin_15_20++;
            } else if (winRate >= 0.10 && winRate < 0.15) {
                cntWin_10_15++;
            } else if (winRate >= 0.05 && winRate < 0.10) {
                cntWin_5_10++;
            } else if (winRate >= 0.00 && winRate < 0.05) {
                cntWin_0_5++;
            } else if (winRate >= -0.05 && winRate < 0.00) {
                cntLose_0_5++;
            } else if (winRate >= -0.10 && winRate < -0.05) {
                cntLose_5_10++;
            } else if (winRate >= -0.15 && winRate < -0.10) {
                cntLose_10_15++;
            } else if (winRate >= -0.20 && winRate < -0.15) {
                cntLose_15_20++;
            } else {
                cntLose_20plus++;
            }
            CLog.info("REPORT", "    timeout: %s, %s, %s, %f",
                pair.getKey(), pair.getValue(), shortTimeoutMap.get(pair), shortTimeoutWinRateMap.get(pair));
        }
        // statistic ------------------------------------------------------------------------------------
        CLog.info("REPORT", "check %d days, check win %f, check lose %f", 
            DAYCOUNT_CHECK, WIN_RATE_CHECK, LOSE_RATE_CHECK);
        CLog.info("REPORT", "short win rate: %f (%d/%d)", 
            getShortWinRate(), shortWinMap.size(), pickList.size());
        CLog.info("REPORT", "short lose rate: %f (%d/%d)",
            getShortLoseRate(), shortLoseMap.size(), pickList.size());
        CLog.info("REPORT", "timeout rate: %f (%d/%d)",
            getUnknownRate(), pickList.size() - shortWinMap.size() - shortLoseMap.size(), pickList.size());
        CLog.info("REPORT", "    timeout win  [0.20, xxxx) : %d", cntWin_20plus);
        CLog.info("REPORT", "    timeout win  [0.15, 0.20) : %d", cntWin_15_20);
        CLog.info("REPORT", "    timeout win  [0.10, 0.15) : %d", cntWin_10_15);
        CLog.info("REPORT", "    timeout win  [0.05, 0.10) : %d", cntWin_5_10);
        CLog.info("REPORT", "    timeout win  [0.00, 0.05) : %d", cntWin_0_5);
        CLog.info("REPORT", "    timeout lose (0.00, 0.05] : %d", cntLose_0_5);
        CLog.info("REPORT", "    timeout lose (0.05, 0.10] : %d", cntLose_5_10);
        CLog.info("REPORT", "    timeout lose (0.10, 0.15] : %d", cntLose_10_15);
        CLog.info("REPORT", "    timeout lose (0.15, 0.20] : %d", cntLose_15_20);
        CLog.info("REPORT", "    timeout lose (0.20, xxxx] : %d", cntLose_20plus);
        CLog.info("REPORT", "-------- PickerReport end --------");
    }

    /* 
     * 检查周期涨幅超过阈值的比例 （短期盈利的个数/选择的个数）
     */
    public double getShortWinRate() {
        return (double)shortWinMap.size() / pickList.size();
    }
    /* 
     * 检查周期跌幅超过阈值的比例 （短期亏损的个数/选择的个数）
     */
    public double getShortLoseRate() {
        return (double)shortLoseMap.size() / pickList.size();
    }
    /* 
     * 检查周期未达到盈利亏损阈值的比例
     */
    public double getUnknownRate() {
        return (double)(pickList.size() - shortWinMap.size() - shortLoseMap.size())/ pickList.size();
    }  
    

    private long DAYCOUNT_CHECK = 20; // 参数：统计N天的表现
    private double WIN_RATE_CHECK = 0.05; // 参数：盈利率检查
    private double LOSE_RATE_CHECK = 0.20; // 参数：亏损率检查

    /* 
     * 选择列表（日期，股票ID）
    */ 
    public List<Entry<String, String>> pickList; 
    /* 
     * 选择key 对应的日K
    */ 
    public Map<Entry<String, String>, KLine> pickKLineMap;

    /* 检查周期内涨幅超过阈值的选择数据 
     * key: (日期，股票ID)
     * value: 涨幅超过阈值的日期
    */ 
    public Map<Entry<String, String>, String> shortWinMap; 
    public Map<Entry<String, String>, KLine> shortWinKLineMap; 

    /* 检查周期内跌幅超过阈值的选择数据 
     * key: (日期，股票ID)
     * value: 跌幅超过阈值的日期
    */ 
    public Map<Entry<String, String>, String> shortLoseMap; 
    public Map<Entry<String, String>, KLine> shortLoseKLineMap; 

    /* 检查周期内未发生超过涨幅跌幅阈值的截止日数据
     * key: (日期，股票ID)
     * value: 跌幅超过阈值的日期
    */ 
    public Map<Entry<String, String>, String> shortTimeoutMap; 
    public Map<Entry<String, String>, KLine> shortTimeoutKLineMap; 
    public Map<Entry<String, String>, Double> shortTimeoutWinRateMap; 
}
