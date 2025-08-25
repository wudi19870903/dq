package pers.di.account;

import java.util.*;

import pers.di.account.*;
import pers.di.common.*;

/*
 * implement market operation interface
 * 
 * user could registerDealReplier to receive the deal information
 * user call postTradeRequest and wait to market reply
 * user could implement IMarketOpe IF 
 * 
 */
abstract public class IMarketOpe {
	
	public static abstract class IMarketDealReplier {
		public abstract void onDeal(TRANACT tranact, String stockID, int amount, double price, double cost);
	}
	
	
	/*
	 * 启动市场操作接口
	 */
	abstract public int start();
	
	/*
	 * 停止市场操作接口
	 */
	abstract public int stop();
	
	/*
	 * 抛送市场交易请求
	 * param tranact: transaction action
	 * id: stock ID
	 * amount: transaction amount
	 * price: transaction price
	 * 
	 * 此方法需要市场方实现
	 * 成交后调用 dealReply方法，回调给用户成交信息
	 */
	abstract public int postTradeRequest(TRANACT tranact, String id, int amount, double price); 
	
	/*
	 * call this, if market deal!
	 */
	public int dealReply(TRANACT tranact, String id, int amount, double price, double cost)
	{
		if(null != m_cIMarketDealReplier)
		{
			m_cIMarketDealReplier.onDeal(tranact, id, amount, price, cost);
		}
		return 0;
	}
	
	
	/*
	 * ********************************************************************************************
	 */
	
	public IMarketOpe()
	{
		m_cIMarketDealReplier = null;
	}
	
	public int registerDealReplier(IMarketDealReplier dealReplier) 
	{
		m_cIMarketDealReplier = dealReplier;
		return 0;
	}
	
	private IMarketDealReplier m_cIMarketDealReplier;
}