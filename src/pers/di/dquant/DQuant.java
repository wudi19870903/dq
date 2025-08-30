package pers.di.dquant;

import pers.di.common.CListObserver;
import pers.di.dataengine.DAContext;
import pers.di.dataengine.StockDataEngine;
import pers.di.dataengine.internal.StockDataEngineImpl;
import pers.di.dquant.internal.DQuantImpl;
import pers.di.model.KLine;

public class DQuant {

    /**
     * 运行用户自定义的选股策略进行分析
     * @param triggerCfgStr 触发配置，此字段参考dataengine中的TriggerMode字段含义
     *                      如：HistoryTest 2024-09-01 2025-01-02
     * @param strategy 选股策略
     */
    public void runUserPickAnalysis(String triggerCfgStr, IStockPickStrategy strategy, PickerReport report) {}

    // singleton
    private static volatile DQuant instance;
    protected DQuant() {
    }
    public static DQuant getInstance() {
        if (instance == null) {
            synchronized (DQuant.class) {
                if (instance == null) {
                    instance = new DQuantImpl();
                }
            }
        }
        return instance;
    }
}
