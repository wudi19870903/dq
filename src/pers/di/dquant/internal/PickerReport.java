package pers.di.dquant.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.dataengine.DAContext;
import pers.di.model.KLine;
import pers.di.model.StockUtils;

public class PickerReport {
    private long DAYCOUNT_CHECK = 20; // 参数：统计N天的表现
    private double WIN_RATE_CHECK = 0.05; // 参数：盈利率检查
    private double LOSE_RATE_CHECK = 0.20; // 参数：亏损率检查

    public PickerReport () {
        pickList = new ArrayList<Pair<String, String>>();
        pickKLineMap = new java.util.HashMap<Pair<String, String>, KLine>();
        shortWinMap = new java.util.HashMap<Pair<String, String>, String>();
        shortLoseMap = new java.util.HashMap<Pair<String, String>, String>();
    }

    public void onTradingDayFinish(DAContext context) {
        // 统计选入列表的后期表现
        // 如：10天内，出现亏损5%，出现盈利5%，进行统计
        for (int i = 0; i < this.pickList.size(); i++) {
            Pair<String, String> pair = this.pickList.get(i);
            String datePick = pair.getKey();
            String stockID = pair.getValue();
            CListObserver<KLine> klineList = context.getDayKLines(stockID);
            if (null == klineList) {
                continue;
            }
            KLine kline = klineList.end();
            if (StockUtils.getDayCountBetweenBeginEnd(klineList, datePick, kline.date) < DAYCOUNT_CHECK) {
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
                    CLog.info("DQUANT", "StockPickStrategy date:%s stockID:%s shortLose %s", 
                        pair.getKey(), pair.getValue(), kline.date);
                } else if (winRate >= WIN_RATE_CHECK) {
                    this.shortWinMap.put(pair, kline.date);
                    CLog.info("DQUANT", "StockPickStrategy date:%s stockID:%s shortWin %s", 
                        pair.getKey(), pair.getValue(), kline.date);
                } else {
                    // do nothing
                }
            }
        }
    }

    public void reset() {
        pickList.clear();
    }

    public void dump() { 
        CLog.info("REPORT", "-------- PickerReport dump --------");
        CLog.info("REPORT", "pick list:");
        for (Pair<String, String> pair : pickList) {
            CLog.info("REPORT", "    pick: %s, %s", pair.getKey(), pair.getValue());
        }
        CLog.info("REPORT", "short win list:");
        for (Pair<String, String> pair : shortWinMap.keySet()) {
            CLog.info("REPORT", "    short win: %s, %s, %s", pair.getKey(), pair.getValue(), shortWinMap.get(pair));
        }
        CLog.info("REPORT", "short lose list:");
        for (Pair<String, String> pair : shortLoseMap.keySet()) {
            CLog.info("REPORT", "    short lose: %s, %s, %s", pair.getKey(), pair.getValue(), shortLoseMap.get(pair));
        }
        CLog.info("REPORT", "check %d days, check win %f, check lose %f", 
            DAYCOUNT_CHECK, WIN_RATE_CHECK, LOSE_RATE_CHECK);
        CLog.info("REPORT", "short win rate: %f (%d/%d)", 
            getShortWinRate(), shortWinMap.size(), pickList.size());
        CLog.info("REPORT", "short lose rate: %f (%d/%d)",
            getShortLoseRate(), shortLoseMap.size(), pickList.size());
        CLog.info("REPORT", "unknown rate: %f (%d/%d)",
            getUnknownRate(), pickList.size() - shortWinMap.size() - shortLoseMap.size(), pickList.size());
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
    
    /* 
     * 选择列表（日期，股票ID）
    */ 
    public List<Pair<String, String>> pickList; 
    public Map<Pair<String, String>, KLine> pickKLineMap;
    /* 检查周期内涨幅超过阈值的选择数据 
     * key: (日期，股票ID)
     * value: 涨幅超过阈值的日期
    */ 
    public Map<Pair<String, String>, String> shortWinMap; 
    /* 检查周期内跌幅超过阈值的选择数据 
     * key: (日期，股票ID)
     * value: 跌幅超过阈值的日期
    */ 
    public Map<Pair<String, String>, String> shortLoseMap; 
}
