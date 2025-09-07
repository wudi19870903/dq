package pers.di.dquant.internal;

import pers.di.dataengine.StockDataEngine;
import pers.di.dquant.DQuant;
import pers.di.dquant.IStockPickStrategy;

public class DQuantImpl extends DQuant {
    public DQuantImpl() {
        super();
        mStockDataEngine = StockDataEngine.getInstance();
        mPickerDataEngineListener = new PickerDataEngineListener();
        mTransactionDataEngineListener = new TransactionDataEngineListener();
    }

    @Override
    public void runUserPickAnalysis(String triggerCfgStr, IStockPickStrategy strategy, PickerReport report) {
        mPickerDataEngineListener.setStockPickStrategy(strategy);
        mPickerDataEngineListener.setPickerReport(report);
        mStockDataEngine.config("TriggerMode", triggerCfgStr);
        mStockDataEngine.registerListener(mPickerDataEngineListener);
        mStockDataEngine.run();
    }

    @Override
    public void runUserTransactionAnalysis(String triggerCfgStr, IStockPickStrategy strategy) {
        mTransactionDataEngineListener.setStockPickStrategy(strategy);
        mStockDataEngine.config("TriggerMode", triggerCfgStr);
        mStockDataEngine.registerListener(mTransactionDataEngineListener);
        mStockDataEngine.run();
    }

    private StockDataEngine mStockDataEngine;
    private PickerDataEngineListener mPickerDataEngineListener;
    private TransactionDataEngineListener mTransactionDataEngineListener;
}
