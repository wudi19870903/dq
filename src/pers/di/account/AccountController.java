package pers.di.account;

import java.util.*;

import pers.di.account.*;
import pers.di.account.internal.*;
import pers.di.account.internal.AccountImpl;
import pers.di.common.CLog;
import pers.di.common.CSyncObj;
import pers.di.common.CThread;
import pers.di.common.CUtilsDateTime;

public class AccountController {
	/* 
	 * construct
	 * param: 
	 *     dataRoot: account file root dir
	 */
	public AccountController(String dataRoot)
	{
		m_cSync = new CSyncObj();
		m_dataRoot = dataRoot;
		m_accountEntity = null;
		m_FlushThread = new FlushThread(this);
		m_MarketOpe = null;
		m_innerMarketReplier = null;
	}
	
	/* 
	 * load account entity from local file
	 * param: 
	 *     accID: account id ref to file name
	 *     bCreate: is create new file or not
	 *     cIMarketOpe: invoke object for market ope
	 * notes:
	 *     if bCreate is true, will try create new account when open account failed
	 */
	public int open(String ID, IMarketOpe cIMarketOpe, boolean bCreate)
	{
		m_cSync.Lock();
		
		// init m_accountEntity m_innerMarketReplier
		if(null != cIMarketOpe)
		{
			m_MarketOpe = cIMarketOpe;
			m_innerMarketReplier = new InnerMarketReplier(this);
			if( 0 != m_MarketOpe.registerDealReplier(m_innerMarketReplier))
			{
				CLog.error("ACCOUNT", "AccoutDriver init cIMarketOpe.registerDealReplier failed\n");
			}
			
			m_accountEntity =  new AccountImpl();
			if(0 != m_accountEntity.load(m_dataRoot, ID, bCreate, m_MarketOpe))
			{
				CLog.error("ACCOUNT", "AccoutDriver init m_accountEntity.load failed\n");
			}
		}
		else
		{
			CLog.error("ACCOUNT", "AccoutDriver init IMarketOpe failed\n");
		}

		m_cSync.UnLock();
		
		m_FlushThread.startThread();
		if(null != m_MarketOpe)
		{
			m_MarketOpe.start();
		}
		
		return 0;
	}
	public int open(String ID, boolean bCreate) {
		return open(ID, new DefaultMockSyncMarketOpe(), bCreate);
	}
	
	/* 
	 * stop account
	 * notes:
	 *     stop market ope & stop flush file thread
	 */
	public int close()
	{
		if(null != m_MarketOpe)
		{
			m_MarketOpe.stop();
		}
		m_FlushThread.stopThread();
		return 0;
	}
	
	/* 
	 * reset account
	 * param: 
	 *     fInitMoney : the init money for the account
	 */
	public int reset(double fInitMoney)
	{
		int iRet = -1;
		m_cSync.Lock();
		iRet = m_accountEntity.reset(fInitMoney);
		m_cSync.UnLock();
		return iRet;
	}
	
	/* 
	 * set date time for the account
	 */
	public int setDateTime(String date, String time)
	{
		int iRet = -1;
		m_cSync.Lock();
		if(null != m_accountEntity) 
		{
			iRet = m_accountEntity.setDateTime(date, time);
		}
		m_cSync.UnLock();
		return iRet;
	}
	
	/* 
	 * get account interface for user
	 */
	public IAccount account()
	{
		IAccount acc = null;
		m_cSync.Lock();
		if(null != m_accountEntity)
		{
			acc = m_accountEntity;
		}
		m_cSync.UnLock();
		return acc;
	}
	
	/* 
	 * flush current price
	 */
	public int flushCurrentPrice(String stockID, double price)
	{
		int iRet = -1;
		m_cSync.Lock();
		if(null != m_accountEntity) 
		{
			iRet = m_accountEntity.flushCurrentPrice(stockID, price);
		}
		m_cSync.UnLock();
		return iRet;
	}
	
	/* 
	 * new day begin
	 */
	public int newDayBegin()
	{
		int iRet = -1;
		m_cSync.Lock();
		if(null != m_accountEntity) 
		{
			iRet = m_accountEntity.newDayBegin();
		}
		m_cSync.UnLock();
		return iRet;
	}
	
	/* 
	 * new day end
	 */
	public int newDayEnd()
	{
		int iRet = -1;
		m_cSync.Lock();
		if(null != m_accountEntity) 
		{
			iRet = m_accountEntity.newDayEnd();
		}
		m_cSync.UnLock();
		return iRet;
	}
	
	/*
	 * ************************************************************************************************
	 */
		
	private static class InnerMarketReplier extends IMarketOpe.IMarketDealReplier
	{
		public InnerMarketReplier(AccountController cAccountController)
		{
			m_cAccountController = cAccountController;
		}
		@Override
		public void onDeal(TRANACT tranact, String id, int amount, double price, double cost) {
			m_cAccountController.onDeal(tranact, id, amount, price, cost);
		}	
		private AccountController m_cAccountController;
	}

	private void onDeal(TRANACT tranact, String id, int amount, double price, double cost) 
	{
		m_cSync.Lock();
		if(null != m_accountEntity)
		{
			m_accountEntity.onDeal(tranact, id, amount, price, cost);
		}
		m_cSync.UnLock();
	}

	private int commit()
	{
		int iRet = -1;
		m_cSync.Lock();
		if(null != m_accountEntity) 
		{
			iRet = m_accountEntity.commit();
		}
		m_cSync.UnLock();
		return iRet;
	}
	
	private static class FlushThread extends CThread
	{
		public FlushThread(AccountController cAccountController)
		{
			m_lastFlushTC = 0;
			m_cAccountController = cAccountController;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!checkQuit())
			{
				this.msleep(1000);
				long lCurTC = CUtilsDateTime.GetCurrentTimeMillis();
				if(lCurTC - m_lastFlushTC > 1000*15)
				{
					m_cAccountController.commit();
					m_lastFlushTC = lCurTC;
				}
			}
		}
		private long m_lastFlushTC;
		private AccountController m_cAccountController;
	}
	
	
	private CSyncObj m_cSync; 
	private String m_dataRoot;
	private AccountImpl m_accountEntity;
	private FlushThread m_FlushThread;
	private IMarketOpe m_MarketOpe;
	private InnerMarketReplier m_innerMarketReplier;
}
