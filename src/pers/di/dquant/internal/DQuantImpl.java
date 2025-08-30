package pers.di.dquant.internal;

import pers.di.dataengine.StockDataEngine;
import pers.di.dquant.DQuant;
import pers.di.dquant.IStockPickStrategy;
import pers.di.dquant.PickerReport;

public class DQuantImpl extends DQuant {
    public DQuantImpl() {
        super();
        mStockDataEngine = StockDataEngine.getInstance();
        mPickerDataEngineListener = new PickerDataEngineListener();
    }

    @Override
    public void runUserPickAnalysis(String triggerCfgStr, IStockPickStrategy strategy, PickerReport report) {
        mPickerDataEngineListener.setStockPickStrategy(strategy);
        mPickerDataEngineListener.setPickerReport(report);
        mStockDataEngine.config("TriggerMode", triggerCfgStr);
        mStockDataEngine.registerListener(mPickerDataEngineListener);
        mStockDataEngine.run();
    }

    private StockDataEngine mStockDataEngine;
    private PickerDataEngineListener mPickerDataEngineListener;
}
