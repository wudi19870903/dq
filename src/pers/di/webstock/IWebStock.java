package pers.di.webstock;

import java.util.List;

public interface IWebStock {

	public class StockItem
	{
		public String ID;    // e.g. 300163
		public String name;  // e.g. 先锋新材
	}
	public class StockInfo
	{
		public String name;                  // 先锋新材
		public String date;
		public String time;
		public double curPrice;
		public double allMarketValue;        // 总市值, unit 亿
		public double circulatedMarketValue; // 流通市值： unit 亿
		public double peRatio;
	}
	public class DividendPayout
	{
		public String date; 
		public double songGu; // 送股
		public double zhuanGu; // 转送
		public double paiXi; // 派息
	}
	public class KLine
	{
		public String date; // e.g. 2015-09-18
		public String time; // e.g. 13:25:20
		public double open;
		public double close;
		public double low;
		public double high;
		public double volume; // 单位手
	}
	public class TransactionRecord
	{
		public String time; // e.g. 13:25:20
		public double price; 
		public double volume; // 单位手
	}
	public class RealTimeInfoLite
	{
		public String stockID;
		public String name;
		public String date;
		public String time;
		public double curPrice;
	}
	
	public int getDividendPayout(String stockID, List<DividendPayout> container);
	public int getKLine(String stockID, List<KLine> container);

	// public int getAllStockList(List<StockItem> container);
	// public int getStockInfo(String stockID, StockInfo container);
	// public int getTransactionRecordHistory(String stockID, String date, List<TransactionRecord> container);
	// public int getRealTimeInfo(List<String> stockIDs, List<RealTimeInfoLite> container);
}