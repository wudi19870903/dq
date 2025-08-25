package pers.di.account;

public class DealOrder {
	
	public String date;
	public String time;
	public String stockID;
	public TRANACT tranAct;
	public int amount; // 成交数量
	public double price; // 成交价格
	
	public double cost; // 成交费用(买入时为买入费用，卖出时为平均买入和卖出费用之和)
	
	public void CopyFrom(DealOrder c)
	{
		date = c.date;
		time = c.time;
		stockID = c.stockID;
		tranAct = c.tranAct;
		amount = c.amount;
		price = c.price;
	}
}
