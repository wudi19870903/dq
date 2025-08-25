package pers.di.account.internal;

import pers.di.account.IMarketOpe;
import pers.di.account.TRANACT;

public class DefaultMockSyncMarketOpe extends IMarketOpe {
	@Override
	public int start() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int stop() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int postTradeRequest(TRANACT tranact, String id, int amount, double price) {
		// TODO Auto-generated method stub
		
		// 过户费
		double fTransferFee = s_transactionCostsRatio_TransferFee * amount * price;
		// 佣金
		double fPoundage = s_transactionCostsRatio_Poundage * amount * price;
		if(fPoundage < s_transactionCosts_MinPoundage)
		{
			fPoundage = s_transactionCosts_MinPoundage;
		}
		// 本次卖出印花税
		double fSellStampDuty = 0;
		if(TRANACT.SELL == tranact)
		{
			fSellStampDuty = s_transactionCostsRatio_Sell_StampDuty * amount * price;
		}
		
		double cost = fTransferFee + fPoundage + fSellStampDuty;
		super.dealReply(tranact, id, amount, price, cost);
		
		return 0;
	}

    public static double calcBuyCost(long amount, double price) {
        // 过户费
		double fTransferFee = s_transactionCostsRatio_TransferFee * amount * price;
		// 佣金
		double fPoundage = s_transactionCostsRatio_Poundage * amount * price;
		if(fPoundage < s_transactionCosts_MinPoundage)
		{
			fPoundage = s_transactionCosts_MinPoundage;
		}
		// 本次卖出印花税
		double fSellStampDuty = 0;
		double cost = fTransferFee + fPoundage + fSellStampDuty;
        return cost;
    }

    public static double calcSellCost(long amount, double price) {
        // 过户费
		double fTransferFee = s_transactionCostsRatio_TransferFee * amount * price;
		// 佣金
		double fPoundage = s_transactionCostsRatio_Poundage * amount * price;
		if(fPoundage < s_transactionCosts_MinPoundage)
		{
			fPoundage = s_transactionCosts_MinPoundage;
		}
		// 本次卖出印花税
		double fSellStampDuty = s_transactionCostsRatio_Sell_StampDuty * amount * price;
		double cost = fTransferFee + fPoundage + fSellStampDuty;
        return cost;
    }
	
	public static double s_transactionCostsRatio_TransferFee = 0.00002; // 过户费比率(买卖双边收取)
	public static double s_transactionCostsRatio_Poundage = 0.00025; // 手续费比率-佣金(买卖双边收取)
	public static double s_transactionCosts_MinPoundage = 5.0; // 手续费最小值
	public static double s_transactionCostsRatio_Sell_StampDuty = 0.001; // 印花税比率(卖单边收取)
}
