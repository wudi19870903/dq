package pers.di.account.internal;

import java.math.BigDecimal;
import java.util.*;

import pers.di.account.IAccount;
import pers.di.account.IMarketOpe;
import pers.di.account.*;
import pers.di.account.internal.AccountStore.StoreEntity;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSyncObj;
import pers.di.common.CSystem;
import pers.di.common.CUtilsDateTime;
import pers.di.common.CUtilsMath;

public class AccountImpl implements IAccount {
	

	@Override
	public String ID() {
		
		this.aceessLock();
		
		String retID = null;
		if(m_initFlag) 
		{
			retID = m_accountStore.accountID();
		}

		this.aceessUnLock();
		
		return retID;
	}
	
	@Override
	public String date()
	{
		this.aceessLock();
		
		String retDate = null;
		if(m_initFlag) 
		{
			 retDate = m_accountStore.storeEntity().date;
		}
		
		this.aceessUnLock();
		
		return retDate;
	}
	
	@Override
	public String time()
	{
		this.aceessLock();
		
		String retTime = null;
		if(m_initFlag)
		{
			retTime = m_accountStore.storeEntity().time;
		}
		
		this.aceessUnLock();
		
		return retTime;
	}

	@Override
	public int getMoney(CObjectContainer<Double> ctnMoney) {
		
		this.aceessLock();
		
		int ret = -1;
		
		if(m_initFlag)
		{
			double availableMoney = 0.0;
			availableMoney = m_accountStore.storeEntity().money;
			for(int i=0; i<m_accountStore.storeEntity().commissionOrderList.size(); i++)
			{
				CommissionOrder cCommissionOrder = m_accountStore.storeEntity().commissionOrderList.get(i);
				if(TRANACT.BUY == cCommissionOrder.tranAct)
				{
					double lockedMoney = cCommissionOrder.price * (cCommissionOrder.amount-cCommissionOrder.dealAmount);
					availableMoney = availableMoney-lockedMoney;
				}
			}
			ctnMoney.set(availableMoney);
			ret = 0;
		}

		this.aceessUnLock();
		
		return ret;
	}
	
	@Override
	public int postTradeOrder(TRANACT tranact, String stockID, int amount, double price)
	{
		this.aceessLock();
		
		// reset price 4-5ignore
		price = (double)CUtilsMath.saveNDecimal(price, 3);
		
		if(!m_initFlag) 
		{
			this.aceessUnLock();
			return -1;
		}
		
		CLog.debug("ACCOUNT", "@AccountEntity CommissionOrder [%s %s] [%s %s %d %.3f]",
				m_accountStore.storeEntity().date, m_accountStore.storeEntity().time,
				tranact.toString(), stockID, amount, price);

		if(tranact == TRANACT.BUY)
		{
			// check input param
			if(0 != amount%100 || amount<=0)
			{
				CLog.error("ACCOUNT", "@AccountEntity CommissionOrder amount error! amount:%d", amount);
				this.aceessUnLock();
				return -1;
			}
			CObjectContainer<Double> money = new CObjectContainer<Double>();
			this.getMoney(money);
			double costReservedMoney = amount*price*0.01 < 10? 10.0:amount*price*0.01;
			if(Double.compare(money.get(), amount*price + costReservedMoney) < 0) // check money: stockMoney+reservedMoney
			{
				CLog.info("ACCOUNT", "@AccountEntity CommissionOrder money error! Please reserved stock money and cost money!");
				this.aceessUnLock();
				return -1;
			}
			
			// create commission order
			CommissionOrder cCommissionOrder = new CommissionOrder();
			cCommissionOrder.date = m_accountStore.storeEntity().date;
			cCommissionOrder.time = m_accountStore.storeEntity().time;
			cCommissionOrder.tranAct = TRANACT.BUY;
			cCommissionOrder.stockID = stockID;
			cCommissionOrder.amount = amount;
			cCommissionOrder.price = price;
			m_accountStore.storeEntity().commissionOrderList.add(cCommissionOrder);
			m_accountStore.sync2File();
			
			this.aceessUnLock();
			
			m_cIMarketOpe.postTradeRequest(TRANACT.BUY, stockID, amount, price);

			return 0;
		}
		
		if(tranact == TRANACT.SELL)
		{
			// check input param
			if(amount<=0)
			{
				CLog.error("ACCOUNT", "@AccountEntity CommissionOrder amount error!");
				this.aceessUnLock();
				return -1;
			}
			boolean bCheck = false;
			for(int i=0; i<m_accountStore.storeEntity().holdStockList.size(); i++)
			{
				HoldStock cHoldStock = m_accountStore.storeEntity().holdStockList.get(i);
				if(cHoldStock.stockID.equals(stockID) && cHoldStock.availableAmount>=amount)
				{
					bCheck = true;
					break;
				}
			}
			if(!bCheck)
			{
				CLog.info("ACCOUNT", "@AccountEntity CommissionOrder availableAmount error!");
				this.aceessUnLock();
				return -1;
			}
						
			// create commission order
			CommissionOrder cCommissionOrder = new CommissionOrder();
			cCommissionOrder.date = m_accountStore.storeEntity().date;
			cCommissionOrder.time = m_accountStore.storeEntity().time;
			cCommissionOrder.tranAct = TRANACT.SELL;
			cCommissionOrder.stockID = stockID;
			cCommissionOrder.amount = amount;
			cCommissionOrder.price = price;
			m_accountStore.storeEntity().commissionOrderList.add(cCommissionOrder);
			m_accountStore.sync2File();
			
			m_cIMarketOpe.postTradeRequest(TRANACT.SELL, stockID, amount, price);
			this.aceessUnLock();
			return 0;
		}
		
		this.aceessUnLock();
		return -1;
	}

	@Override
	public int getCommissionOrderList(List<CommissionOrder> ctnList) {
		
		this.aceessLock();
		
		if(!m_initFlag) 
		{
			this.aceessUnLock();
			return -1;
		}
		
		ctnList.clear();
		ctnList.addAll(m_accountStore.storeEntity().commissionOrderList);
		
		this.aceessUnLock();
		return 0;
	}
	
	@Override
	public int getDealOrderList(List<DealOrder> ctnList)
	{
		this.aceessLock();
		
		if(!m_initFlag) 
		{
			this.aceessUnLock();
			return -1;
		}
		
		ctnList.clear();
		ctnList.addAll(m_accountStore.storeEntity().dealOrderList);
		
		this.aceessUnLock();
		return 0;
	}

	@Override
	public int getHoldStockList(List<HoldStock> ctnList) {
		
		this.aceessLock();
		
		if(!m_initFlag) 
		{
			this.aceessUnLock();
			return -1;
		}
		
		ctnList.clear();
		ctnList.addAll(m_accountStore.storeEntity().holdStockList);
		
		this.aceessUnLock();
		return 0;
	}
	

	/*
	 * ******************************************************************************************
	 */
	
	public AccountImpl()
	{
		m_cSync = new CSyncObj();
		m_cIMarketOpe = null;
		m_accountStore = null;
		m_initFlag = false;
	}
	
	public int load(String dataRoot, String accID, boolean bCreate, IMarketOpe cIMarketOpe)
	{
		this.aceessLock();
		
		m_cIMarketOpe = cIMarketOpe;
		
		m_accountStore = new AccountStore(dataRoot, accID);
		if(!m_accountStore.sync2Mem())
		{
			if(bCreate)
			{
				boolean iStoreInit = m_accountStore.storeInit();
				if(iStoreInit)
				{
					m_initFlag = true;
					
					this.aceessUnLock();
					return 0;
				}
				else
				{
					CLog.debug("ACCOUNT", "@AccountEntity initialize AccountID:%s err!", accID);
					
					this.aceessUnLock();
					return -1;
				}
			}
			else
			{
				CLog.debug("ACCOUNT", "@AccountEntity initialize AccountID:%s err!", accID);
				
				this.aceessUnLock();
				return -1;
			}
		}
		
		m_initFlag = true;
		CLog.debug("ACCOUNT", "@AccountEntity initialize AccountID:%s OK", accID);
		
		this.aceessUnLock();
		return 0;
	}
	
	public int setDateTime(String date, String time)
	{
		this.aceessLock();
		
		if(!m_initFlag) 
		{
			this.aceessUnLock();
			return -1;
		}

		m_accountStore.storeEntity().date = date;
		m_accountStore.storeEntity().time = time;
		
		this.aceessUnLock();
		return 0;
	}
	
	public int flushCurrentPrice(String stockID, double price)
	{
		this.aceessLock();
		
		// reset price 4-5ignore
		price = (double)CUtilsMath.saveNDecimal(price, 3);
				
		if(!m_initFlag) 
		{
			this.aceessUnLock();
			return -1;
		}
		
		for(int i=0; i< m_accountStore.storeEntity().holdStockList.size(); i++)
		{
			HoldStock cHoldStock = m_accountStore.storeEntity().holdStockList.get(i);
			if(stockID.equals(cHoldStock.stockID))
			{
				//cHoldStock.curPrice = CUtilsMath.saveNDecimal(price, 3);
				cHoldStock.curPrice = price;
				
			}
		}
		
		this.aceessUnLock();
		return 0;
	}
	
	public int commit()
	{
		this.aceessLock();
		m_accountStore.sync2File();
		this.aceessUnLock();
		return 0;
	}
	
	public int newDayBegin() {
		
		this.aceessLock();
		this.aceessUnLock();
		
		return 0;
	}

	public int newDayEnd() {
		
		this.aceessLock();
		
		if(!m_initFlag) 
		{
			this.aceessUnLock();
			return -1;
		}
		
		// 清空委托表
		m_accountStore.storeEntity().commissionOrderList.clear();
		
		// 清空成交表
		m_accountStore.storeEntity().dealOrderList.clear();

		// 股票全部可卖
		for(int i=0; i< m_accountStore.storeEntity().holdStockList.size(); i++)
		{
			m_accountStore.storeEntity().holdStockList.get(i).availableAmount = m_accountStore.storeEntity().holdStockList.get(i).totalAmount;
		}
		
		m_accountStore.sync2File();
		
		
		this.aceessUnLock();
		return 0; 
	}
	
	public void onDeal(TRANACT tranact, String stockID, int amount, double price, double cost) 
	{
		this.aceessLock();
		
		// reset price 4-5ignore
		price = (double)CUtilsMath.saveNDecimal(price, 3);
		cost = (double)CUtilsMath.saveNDecimal(cost, 3);
				
		// commission order update
		Collections.sort(m_accountStore.storeEntity().commissionOrderList);
		int leftDistribution = amount;
		for(int i=0; i<m_accountStore.storeEntity().commissionOrderList.size(); i++)
		{
			CommissionOrder cCommissionOrder = m_accountStore.storeEntity().commissionOrderList.get(i);
			if(cCommissionOrder.tranAct==tranact && cCommissionOrder.stockID.equals(stockID))
			{
				int currentNeedDistribution = cCommissionOrder.amount-cCommissionOrder.dealAmount;
				if(currentNeedDistribution > 0)
				{
					if(leftDistribution <= currentNeedDistribution)
					{
						cCommissionOrder.dealAmount = cCommissionOrder.dealAmount+leftDistribution;
						leftDistribution = 0;
						break;
					}
					else
					{
						cCommissionOrder.dealAmount = cCommissionOrder.amount;
						leftDistribution = leftDistribution-currentNeedDistribution;
					}
				}
			}
		}
		
		// hold stock update
		if(tranact == TRANACT.BUY)
		{
			// 获取持有对象
			HoldStock cHoldStock = null;
			for(int i = 0; i< m_accountStore.storeEntity().holdStockList.size(); i++)
			{
				HoldStock cTmpHoldStock = m_accountStore.storeEntity().holdStockList.get(i);
				if(cTmpHoldStock.stockID.equals(stockID))
				{
					cHoldStock = cTmpHoldStock;
					break;
				}
			}
			if(null == cHoldStock)
			{
				HoldStock cNewHoldStock = new HoldStock();
				cNewHoldStock.createDate = m_accountStore.storeEntity().date;
				cNewHoldStock.stockID = stockID;
				cNewHoldStock.totalAmount = 0;
				cNewHoldStock.availableAmount = 0;
				cNewHoldStock.curPrice = 0;
				cNewHoldStock.refPrimeCostPrice = 0;
				cHoldStock = cNewHoldStock;
				m_accountStore.storeEntity().holdStockList.add(cNewHoldStock);
			}
			
			// 重置对象 (交易费用直接体现在参考成本价里)
			int oriTotalAmount = cHoldStock.totalAmount;
			cHoldStock.totalAmount = cHoldStock.totalAmount + amount;
			cHoldStock.availableAmount = cHoldStock.availableAmount;
			cHoldStock.curPrice = price;
			cHoldStock.refPrimeCostPrice = 
					(cHoldStock.refPrimeCostPrice*oriTotalAmount + price*amount + cost) / cHoldStock.totalAmount;
			cHoldStock.refPrimeCostPrice = CUtilsMath.saveNDecimal(cHoldStock.refPrimeCostPrice, 3);
			
			// 更新 money
			m_accountStore.storeEntity().money = m_accountStore.storeEntity().money - price*amount - cost;
			m_accountStore.storeEntity().money = CUtilsMath.saveNDecimal(m_accountStore.storeEntity().money, 3);
			if(m_accountStore.storeEntity().money < 0)
			{
				CLog.error("ACCOUNT", "BuyDeal Not have enough money to pay the stockmoney:%.3f cost:%.3f !! currentMoney:%.3f", 
						price*amount, cost, m_accountStore.storeEntity().money);
				CSystem.exit(-1);
			}
		}
		else if(tranact == TRANACT.SELL)
		{
			HoldStock cHoldStock = null;
			for(int i = 0; i< m_accountStore.storeEntity().holdStockList.size(); i++)
			{
				HoldStock cTmpHoldStock = m_accountStore.storeEntity().holdStockList.get(i);
				if(cTmpHoldStock.stockID.equals(stockID))
				{
					cHoldStock = cTmpHoldStock;
					break;
				}
			}
			
			if(null != cHoldStock)
			{
				double sellRawProfit = (price - cHoldStock.refPrimeCostPrice)*amount; // 卖出裸利润
				
				// 重置对象 (交易费用在卖出价钱中扣除)
				cHoldStock.totalAmount = cHoldStock.totalAmount - amount;
				cHoldStock.availableAmount = cHoldStock.availableAmount - amount;
				cHoldStock.curPrice = price;
				
				cHoldStock.refPrimeCostPrice = 
						(cHoldStock.refPrimeCostPrice*cHoldStock.totalAmount + cost - sellRawProfit) / cHoldStock.totalAmount;
				cHoldStock.refPrimeCostPrice = CUtilsMath.saveNDecimal(cHoldStock.refPrimeCostPrice, 3);
				
				m_accountStore.storeEntity().money = 
						m_accountStore.storeEntity().money + price*amount - cost;
				m_accountStore.storeEntity().money = CUtilsMath.saveNDecimal(m_accountStore.storeEntity().money, 3);

				// 清仓
				if(cHoldStock.totalAmount == 0)
				{
					m_accountStore.storeEntity().holdStockList.remove(cHoldStock);
				}
			}
			else
			{
				CLog.error("ACCOUNT", "@AccountEntity.onDeal SELL err!\n");
			}
		}
		
		// deal order
		DealOrder dealOrder = new DealOrder();
		dealOrder.date = m_accountStore.storeEntity().date;
		dealOrder.time = m_accountStore.storeEntity().time;
		dealOrder.tranAct = tranact;
		dealOrder.stockID = stockID;
		dealOrder.amount = amount;
		dealOrder.price = price;
		dealOrder.cost = cost;
		m_accountStore.storeEntity().dealOrderList.add(dealOrder);
				
		CLog.debug("ACCOUNT", "@AccountEntity DealOrder [%s %s] [%s %s %d %.3f %.3f(%.3f) %.3f]",
				m_accountStore.storeEntity().date, m_accountStore.storeEntity().time,
				tranact.toString(), stockID, amount, price,
				amount*price, cost, m_accountStore.storeEntity().money);
		
		m_accountStore.sync2File();
		
		this.aceessUnLock();
	}
	
	public int reset(double fInitMoney)
	{
		this.aceessLock();
		
		m_accountStore.storeEntity().reset(fInitMoney);
		m_accountStore.sync2File();
		
		this.aceessUnLock();
		return 0;
	}
	
	@Override
	public int getTotalAssets(CObjectContainer<Double> ctnTotalAssets)
	{
		this.aceessLock();
		
		int iRet = -1;
		
		CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
		int iRetGetMoney = getMoney(ctnMoney);
		
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		int iRetGetHoldList = getHoldStockList(ctnHoldStockList);
		
		if(0==iRetGetMoney && 0==iRetGetHoldList)
		{
			Double dTotalAssets = ctnMoney.get();
			for(int i=0; i<ctnHoldStockList.size(); i++)
			{
				dTotalAssets += ctnHoldStockList.get(i).totalAmount*ctnHoldStockList.get(i).curPrice;
			}
			ctnTotalAssets.set(dTotalAssets);
			iRet = 0;
		}
		
		this.aceessUnLock();
		
		return iRet;
	}
	
	@Override
	public int getTotalStockMarketValue(CObjectContainer<Double> ctnTotalStockMarketValue)
	{
		this.aceessLock();
		
		int iRet = -1;

		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		int iRetGetHoldList = getHoldStockList(ctnHoldStockList);
		
		if(0==iRetGetHoldList)
		{
			Double dTotalStockMarketValue = 0.0;
			for(int i=0; i<ctnHoldStockList.size(); i++)
			{
				dTotalStockMarketValue += ctnHoldStockList.get(i).totalAmount*ctnHoldStockList.get(i).curPrice;
			}
			ctnTotalStockMarketValue.set(dTotalStockMarketValue);
			iRet = 0;
		}
		
		this.aceessUnLock();
		
		return iRet;
	}
	
	@Override
	public int getHoldStock(String stockID, CObjectContainer<HoldStock> ctnHoldStock)
	{
		this.aceessLock();
		
		List<HoldStock> ctnList = new ArrayList<HoldStock>();
		int iRet = getHoldStockList(ctnList);
		if(0 == iRet)
		{
			for(int i=0; i<ctnList.size(); i++)
			{
				if(ctnList.get(i).stockID.equals(stockID))
				{
					ctnHoldStock.set(ctnList.get(i));
				}
			}
		}
		
		this.aceessUnLock();
		return 0;
	}
	
	@Override
	public String dump()
	{
		this.aceessLock();
		
		String DumpInfo = String.format("---ACCOUNT---INFO--- %s %s", date(), time());
		
		CObjectContainer<Double> totalAssets = new CObjectContainer<Double>();
		this.getTotalAssets(totalAssets);
		CObjectContainer<Double> money = new CObjectContainer<Double>();
		this.getMoney(money);
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		this.getHoldStockList(cHoldStockList);
		List<CommissionOrder> cCommissionOrderList = new ArrayList<CommissionOrder>();
		this.getCommissionOrderList(cCommissionOrderList);
		List<DealOrder> cDealOrderList = new ArrayList<DealOrder>();
		this.getDealOrderList(cDealOrderList);
		
		// 打印资产
		DumpInfo+=String.format("\n    -TotalAssets: %.3f", totalAssets.get());
		DumpInfo+=String.format("\n    -Money: %.3f", money.get());
		double fStockMarketValue = 0.0;
		for(int i=0; i<cHoldStockList.size(); i++ )
		{
			HoldStock cHoldStock = cHoldStockList.get(i);
			fStockMarketValue = fStockMarketValue + cHoldStock.totalAmount*cHoldStock.curPrice;
		}
		DumpInfo+=String.format("\n    -StockMarketValue: %.3f", fStockMarketValue);

		// 打印持股
		for(int i=0; i<cHoldStockList.size(); i++ )
		{
			HoldStock cHoldStock = cHoldStockList.get(i);
			DumpInfo+=String.format("\n    -HoldStock: %s %s %d %d %.3f %.3f %.3f", 
					cHoldStock.createDate,
					cHoldStock.stockID,
					cHoldStock.totalAmount, cHoldStock.availableAmount,
					cHoldStock.refPrimeCostPrice, cHoldStock.curPrice, 
					cHoldStock.totalAmount*cHoldStock.curPrice);
		}
				
		// 打印委托单
		for(int i=0; i<cCommissionOrderList.size(); i++ )
		{
			CommissionOrder cCommissionOrder = cCommissionOrderList.get(i);
			String tranOpe = "BUY"; 
			if(cCommissionOrder.tranAct == TRANACT.SELL ) tranOpe = "SELL";
				
			DumpInfo+=String.format("\n    -CommissionOrder: %s %s %s %s %d %.3f", 
					cCommissionOrder.date, cCommissionOrder.time, tranOpe, cCommissionOrder.stockID, 
					cCommissionOrder.amount, cCommissionOrder.price);
		}
		
		// 打印成交单
		for(int i=0; i<cDealOrderList.size(); i++ )
		{
			DealOrder cDealOrder = cDealOrderList.get(i);
			String tranOpe = ""; 
			if(cDealOrder.tranAct == TRANACT.BUY ) 
			{
				tranOpe = "BUY";
				DumpInfo+=String.format("\n    -DealOrder: %s %s %s %s %d %.3f (BC-%.3f)", 
						cDealOrder.date, cDealOrder.time, tranOpe, cDealOrder.stockID, 
						cDealOrder.amount, cDealOrder.price, cDealOrder.cost);
			}
			else if(cDealOrder.tranAct == TRANACT.SELL)
			{
				tranOpe = "SELL";
				DumpInfo+=String.format("\n    -DealOrder: %s %s %s %s %d %.3f (SC-%.3f)", 
						cDealOrder.date, cDealOrder.time, tranOpe, cDealOrder.stockID, 
						cDealOrder.amount, cDealOrder.price, cDealOrder.cost);
			}
		}
		
		this.aceessUnLock();
		
		return DumpInfo;
	}
	
	private boolean aceessLock()
	{
		return m_cSync.Lock();
	}
	
	private boolean aceessUnLock()
	{
		return m_cSync.UnLock();
	}
	
	/*
	 * ******************************************************************************************
	 */
	
	private CSyncObj m_cSync;
	private IMarketOpe m_cIMarketOpe;
	private AccountStore m_accountStore;
	private boolean m_initFlag;
}
