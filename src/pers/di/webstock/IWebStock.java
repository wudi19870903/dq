package pers.di.webstock;

import java.util.List;
import pers.di.model.DividendPayout;
import pers.di.model.KLine;

public interface IWebStock {

	public int getDividendPayout(String stockID, List<DividendPayout> container);
	public int getKLine(String stockID, int dataLen, List<KLine> container);

	// public int getAllStockList(List<StockItem> container);
	// public int getStockInfo(String stockID, StockInfo container);
	// public int getTransactionRecordHistory(String stockID, String date, List<TransactionRecord> container);
	// public int getRealTimeInfo(List<String> stockIDs, List<RealTimeInfoLite> container);
}