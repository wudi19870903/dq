package pers.di.model;

/*
 * 股票基本信息
 * 名字-当前价-总市值-流通市值-市盈率等（将来扩展为行业等）
 */
public class StockInfo
{
	public String name;
	public String date;
	public String time;
	public double curPrice;
	public double allMarketValue; // 亿
	public double circulatedMarketValue; // 亿
	public double peRatio;
	public StockInfo()
	{
		name = "";
		date = "0000-00-00";
		time = "00:00:00";
		curPrice =0.0f;
		allMarketValue = 0.0f;
		circulatedMarketValue = 0.0f;
		peRatio = 0.0f;
	}
	public void CopyFrom(StockInfo cCopyFromObj)
	{
		name = cCopyFromObj.name;
		date = cCopyFromObj.date;
		time = cCopyFromObj.time;
		curPrice = cCopyFromObj.curPrice;
		allMarketValue = cCopyFromObj.allMarketValue;
		circulatedMarketValue = cCopyFromObj.circulatedMarketValue;
		peRatio = cCopyFromObj.peRatio;
	}
}