package pers.di.account;

public class CommissionOrder implements Comparable {
	
	public String date;
	public String time;
	public String stockID;
	public TRANACT tranAct;
	public int amount; // 委托数量
	public double price; // 委托价格
	public int dealAmount; // 已成交数量
	
	public void CopyFrom(CommissionOrder c)
	{
		date = c.date;
		time = c.time;
		stockID = c.stockID;
		tranAct = c.tranAct;
		amount = c.amount;
		price = c.price;
		dealAmount = c.dealAmount;
	}

	@Override
	public int compareTo(Object o) {
		CommissionOrder c0 = (CommissionOrder)this;
		CommissionOrder c1 = (CommissionOrder)o;
		
		// 先按id排序
		if(!c0.stockID.equals(c1.stockID))
		{
			return c0.stockID.compareTo(c1.stockID);
		}
		
		// 再按买卖排序
		if(TRANACT.BUY == c0.tranAct && TRANACT.SELL == c1.tranAct)
		{
			return -1;
		}
		else if(TRANACT.SELL == c0.tranAct && TRANACT.BUY == c1.tranAct)
		{
			return 1;
		}
		
		// 根据买卖不同 按价格排序
		if(TRANACT.BUY == c0.tranAct)
		{
			if(c0.price < c1.price)
			{
				return 1;
			}
			else if(c0.price > c1.price)
			{
				return -1;
			}
			else
			{
				// 最后按时间排序
				return c0.time.compareTo(c1.time);
			}
		}
		else if(TRANACT.SELL == c0.tranAct)
		{
			if(c0.price < c1.price)
			{
				return -1;
			}
			else if(c0.price > c1.price)
			{
				return 1;
			}
			else
			{
				// 最后按时间排序
				return c0.time.compareTo(c1.time);
			}
		}
		else
		{
			return 0;
		}
		
	}
}
