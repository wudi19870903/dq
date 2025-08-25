package pers.di.account;

public class HoldStock {
	
	/*
	 * 注：参考XX是指针对本次建仓后，经过不断买卖而形成的原始参考数值
	 */
	
	public String createDate; // 建仓日期
	public String stockID; // 股票ID
	public int totalAmount; // 持有总量（股）
	public int availableAmount; // 可卖数量
	public double curPrice; // 当前价
	public double refPrimeCostPrice; // 参考成本价（只做本次仓位建仓后的参考成本价格，未有实际用途）
	
	public HoldStock()
	{
		Clear();
	}
	
	public void Clear()
	{
		createDate = "0000-00-00";
		stockID = "";
		totalAmount = 0;
		availableAmount = 0;
		curPrice = 0.0f;
		refPrimeCostPrice = 0.0f;
	}
	
	public void CopyFrom(HoldStock c)
	{
		createDate = c.createDate;
		stockID = c.stockID;
		totalAmount = c.totalAmount;
		availableAmount = c.availableAmount;
		curPrice = c.curPrice;
		refPrimeCostPrice = c.refPrimeCostPrice;
	}
	
	public double marketValue() {
		return curPrice*totalAmount;
	}
	
	public double refProfit() // 参考利润值（只做本次仓位建仓后的参考盈亏金额，不计算交易费用）
	{
		return (curPrice - refPrimeCostPrice)*totalAmount;
	}
	
	public double refProfitRatio() // 参考利润比（只做本次仓位建仓后的参考盈亏比例）
	{
		return (curPrice - refPrimeCostPrice)/refPrimeCostPrice;
	}
}
