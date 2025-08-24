package pers.di.tools;

import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAContext;
import pers.di.dataengine.IEngineListener;
import pers.di.dataengine.StockDataEngine;
import pers.di.model.KLine;

public class RunDataEngineTest extends IEngineListener {

    @Override
    public void onTradingDayFinish(DAContext context) {
        CLog.info("TEST", "CurrentDate:%s AllStockIDCount:%d",
            context.date(), context.getAllStockID().size());
        for (int i = 0; i < context.getAllStockID().size(); i++) {
            String stockID = context.getAllStockID().get(i);
            CListObserver<KLine> klineList = context.getDayKLines(stockID);
            if (stockID.equals("600000")) {
                CLog.info("TEST", "    stockID:%s latestDate:%s close:%f",
                stockID, klineList.end().date, klineList.end().close);
            }
        }
    }

    public static void main(String[] args) {
		CSystem.start();
        StockDataEngine.getInstance().config("TriggerMode", "HistoryTest 2024-01-01 2025-01-01");
		StockDataEngine.getInstance().registerListener(new RunDataEngineTest());
		StockDataEngine.getInstance().run();
		CSystem.stop();
	}
}
