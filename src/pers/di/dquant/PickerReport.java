package pers.di.dquant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import pers.di.common.CLog;
import pers.di.model.KLine;

public class PickerReport {
    public PickerReport () {
        pickList = new ArrayList<Pair<String, String>>();
        pickKLineMap = new java.util.HashMap<Pair<String, String>, KLine>();
        shortWinMap = new java.util.HashMap<Pair<String, String>, String>();
        shortLoseMap = new java.util.HashMap<Pair<String, String>, String>();
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
        CLog.info("REPORT", "short win rate: %s", getShortWinRate());
        CLog.info("REPORT", "-------- PickerReport end --------");
    }

    /* 
     * 短周期涨幅超过5%的比例 （短期盈利的个数/选择的个数）
     */
    double getShortWinRate() {
        return (double)shortWinMap.size() / pickList.size();
    }
    /* 
     * 短周期跌幅超过5%的比例 （短期亏损的个数/选择的个数）
     */
    double getShortLoseRate() {
        return (double)shortLoseMap.size() / pickList.size();
    }   
    
    /* 
     * 选择列表（日期，股票ID）
    */ 
    public List<Pair<String, String>> pickList; 
    public Map<Pair<String, String>, KLine> pickKLineMap;
    /* 10天内涨幅超过5%的选择数据 
     * key: (日期，股票ID)
     * value: 涨幅超过5%的日期
    */ 
    public Map<Pair<String, String>, String> shortWinMap; 
    /* 10天内跌幅超过5%的选择数据 
     * key: (日期，股票ID)
     * value: 跌幅超过5%的日期
    */ 
    public Map<Pair<String, String>, String> shortLoseMap; 
}
