package pers.di.webstock;

import java.util.List;
import pers.di.model.*;

public interface IWebStock {

	public int getDividendPayout(String stockID, List<DividendPayout> container);
	public int getKLine(String stockID, List<KLine> container);

	// public int getAllStockList(List<StockItem> container);
	// public int getStockInfo(String stockID, StockInfo container);
	// public int getTransactionRecordHistory(String stockID, String date, List<TransactionRecord> container);
	// public int getRealTimeInfo(List<String> stockIDs, List<RealTimeInfoLite> container);
}