package pers.di.dquant.internal;

import javafx.util.Pair;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.dataengine.DAContext;
import pers.di.dataengine.IEngineListener;
import pers.di.dquant.IStockPickStrategy;
import pers.di.model.KLine;
import pers.di.model.StockUtils;

public class PickerDataEngineListener extends IEngineListener {
    @Override
	public void onTradingDayFinish(DAContext context) {
        if (null == mStockPickStrategy) {
            return;
        }
        // 根据策略添加选入列表
        // CLog.info("DQUANT", "PickerDataEngineListener date:%s", context.date());
        for (int i = 0; i < context.getAllStockID().size(); i++) {
            String stockID = context.getAllStockID().get(i);
            CListObserver<KLine> klineList = context.getDayKLines(stockID);
            boolean bPick = mStockPickStrategy.onUserPick(context, stockID, klineList);
            if (bPick) {
                CLog.info("DQUANT", "StockPickStrategy date:%s stockID:%s", 
                    context.date(), stockID);
                Pair<String, String> pickPair = new Pair<String, String>(context.date(), stockID);
                mPickerReport.pickList.add(pickPair);
                mPickerReport.pickKLineMap.put(pickPair, klineList.end());
            }
        }
        // 统计选入列表的后期表现
        mPickerReport.onTradingDayFinish(context);
    }

    public void setStockPickStrategy(IStockPickStrategy strategy) {
        CLog.info("DQUANT", "setStockPickStrategy %s", strategy.getClass().getName());
    	mStockPickStrategy = strategy;
    }
    public void setPickerReport(PickerReport report) {
        CLog.info("DQUANT", "setPickerReport");
    	mPickerReport = report;
        mPickerReport.reset();
    }

    private IStockPickStrategy mStockPickStrategy;
    private PickerReport mPickerReport;
}
