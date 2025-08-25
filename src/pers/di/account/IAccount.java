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
	public abstract String dump();
}
