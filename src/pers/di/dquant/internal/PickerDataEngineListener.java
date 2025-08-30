package pers.di.dquant.internal;

import javafx.util.Pair;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.dataengine.DAContext;
import pers.di.dataengine.IEngineListener;
import pers.di.dquant.IStockPickStrategy;
import pers.di.dquant.PickerReport;
import pers.di.model.KLine;

public class PickerDataEngineListener extends IEngineListener {
    @Override
	public void onTradingDayFinish(DAContext context) {
        if (null == mStockPickStrategy) {
            return;
        }
        //CLog.info("DQUANT", "PickerDataEngineListener date:%s", context.date());
        for (int i = 0; i < context.getAllStockID().size(); i++) {
            String stockID = context.getAllStockID().get(i);
            CListObserver<KLine> klineList = context.getDayKLines(stockID);
            boolean bPick = mStockPickStrategy.onUserPick(context, stockID, klineList);
            if (bPick) {
                CLog.info("DQUANT", "StockPickStrategy date:%s stockID:%s", 
                    context.date(), stockID);
                    mPickerReport.pickList.add(new Pair<String, String>(context.date(), stockID));
            }
        }
    }

    public void setStockPickStrategy(IStockPickStrategy strategy) {
        CLog.info("DQUANT", "setStockPickStrategy");
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
