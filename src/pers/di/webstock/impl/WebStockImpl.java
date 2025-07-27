package pers.di.webstock.impl;

import java.util.List;

import pers.di.webstock.IWebStock;
import pers.di.model.*;
import pers.di.webstock.impl.*;

public class WebStockImpl implements IWebStock {
	
	public WebStockImpl()
	{
		m_DataWebStockAllList = new DataWebStockAllList();
		m_DataWebStockInfo = new DataWebStockInfo();
		m_DataWebStockDayK = new DataWebStockDayK();
		m_TransactionRecordHistory = new TransactionRecordHistory();
		m_DataWebStockRealTimeInfo = new DataWebStockRealTimeInfo();
		m_DataWebStockDividendPayout = new DataWebStockDividendPayout();
	}

	// @Override
	// public int getAllStockList(List<StockItem> container) {
	// 	return m_DataWebStockAllList.getAllStockList(container);
	// }

	// @Override
	// public int getStockInfo(String stockID, StockInfo container) {
	// 	return m_DataWebStockInfo.getStockInfo(stockID, container);
	// }

	@Override
	public int getDividendPayout(String stockID, List<DividendPayout> container) {
		return m_DataWebStockDividendPayout.getDividendPayout(stockID, container);
	}
	
	@Override
	public int getKLine(String stockID, List<KLine> container) {
		return m_DataWebStockDayK.getKLine(stockID, container);
	}

	// @Override
	// public int getTransactionRecordHistory(String stockID, String date, List<TransactionRecord> container) {
	// 	return m_TransactionRecordHistory.getTransactionRecordHistory(stockID, date, container);
	// }

	// @Override
	// public int getRealTimeInfo(List<String> stockIDs, List<RealTimeInfoLite> container) {
	// 	return m_DataWebStockRealTimeInfo.getRealTimeInfo(stockIDs, container);
	// }

	private DataWebStockAllList m_DataWebStockAllList;
	private DataWebStockInfo m_DataWebStockInfo;
	private DataWebStockDayK m_DataWebStockDayK;
	private TransactionRecordHistory m_TransactionRecordHistory;
	private DataWebStockRealTimeInfo m_DataWebStockRealTimeInfo;
	private DataWebStockDividendPayout m_DataWebStockDividendPayout;
}
