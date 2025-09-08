package pers.di.account;

import java.util.ArrayList;
import java.util.List;

import pers.di.common.*;

public interface IAccount {
	/*
	 * postTradeOrder:
	 * transaction cost would deduct from your money, 
	 * so when you post buy order, your must reserve stockMoney & buyCostMoney, else will error exit.
	 */
	// base	
	public abstract String ID();
	public abstract String date();
	public abstract String time();
	public abstract int getMoney(CObjectContainer<Double> ctnMoney);
	public abstract int postTradeOrder(TRANACT tranact, String stockID, int amount, double price);
	public abstract int getCommissionOrderList(List<CommissionOrder> ctnList);
	public abstract int getDealOrderList(List<DealOrder> ctnList);
	public abstract int getHoldStockList(List<HoldStock> ctnList);
	// extend
	public abstract int getTotalAssets(CObjectContainer<Double> ctnTotalAssets);
	public abstract int getTotalStockMarketValue(CObjectContainer<Double> ctnTotalStockMarketValue);
	public abstract int getHoldStock(String stockID, CObjectContainer<HoldStock> ctnHoldStock);
	
	/*
     * -TotalAssets: 总资产（现金+股票市值）
     * -Money: 现金
     * -StockMarketValue: 股票市值
     * -HoldStock: 建仓日期 股票代码 持有量 可用量 参考成本价 当前价 市值
	 * 
     * -TotalAssets: 108653.309
     * -Money: 1118.309
     * -StockMarketValue: 107535.000
     * -HoldStock: 2024-12-25 002468 5000 5000 9.766 9.843 49215.000
     * -HoldStock: 2024-12-25 600000 6000 6000 8.342 9.720 58320.000
	 */
	public abstract String dump();
}
