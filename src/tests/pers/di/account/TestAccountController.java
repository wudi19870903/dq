package pers.di.account;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pers.di.account.*;
import pers.di.common.*;

public class TestAccountController {
	
	public static String s_accountDataRoot = CSystem.getRWRoot() + "\\account";
	
	public static double s_transactionCostsRatioBuy = 0.02f;
	public static double s_transactionCostsRatioSell = 0.05f;
	
	public static class MockMarketOpe extends IMarketOpe
	{
		@Override
		public int start()
		{
			return 0;
		}
		
		@Override
		public int stop()
		{
			return 0;
		}
		
		@Override
		public int postTradeRequest(TRANACT tranact, String id, int amount, double price) {
			if(tranact == TRANACT.BUY)
			{
				super.dealReply(tranact, id, amount, price, amount*price*s_transactionCostsRatioBuy);
			}
			else if(tranact == TRANACT.SELL)
			{
				super.dealReply(tranact, id, amount, price, amount*price*s_transactionCostsRatioSell);
			}
			return 0;
		}
	}
	
	@CTest.test
	public static void test_accountDriver_reset()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" ,  new MockMarketOpe(), true);
		IAccount acc = cAccountController.account();
		
		// set default
		{
			acc.postTradeOrder(TRANACT.BUY, "600001", 100, 1.01f);
			List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
			CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
			CTest.EXPECT_TRUE(ctnHoldList.size() != 0);
		}
		
		// call reset
		cAccountController.reset(10*10000f);
		
		// check
		{
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			CTest.EXPECT_TRUE(acc.getMoney(ctnMoney) == 0);
			CTest.EXPECT_DOUBLE_EQ(ctnMoney.get(), 10*10000f, 2);
			List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
			CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
			CTest.EXPECT_TRUE(ctnHoldList.size() == 0);
		}
		
		cAccountController.close();
	}
	
	@CTest.test
	public static void test_accountDriver_buy()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" ,  new MockMarketOpe(), true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
		
		cAccountController.setDateTime("2017-10-10", "14:00:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 100, 1.60f);
		acc.postTradeOrder(TRANACT.BUY, "600001", 200, 2.00f);
		cAccountController.newDayEnd();
		
		cAccountController.setDateTime("2017-11-11", "13:38:55");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "300002", 500, 10.6f);
		
		// check
		{
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			CTest.EXPECT_TRUE(acc.getMoney(ctnMoney) == 0);
			CTest.EXPECT_DOUBLE_EQ(ctnMoney.get(), 
					10*10000f - 100*1.60f*(1+s_transactionCostsRatioBuy) - 200*2.00f*(1+s_transactionCostsRatioBuy) - 500*10.6f*(1+s_transactionCostsRatioBuy),
					2);
			List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
			CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
			CTest.EXPECT_TRUE(ctnHoldList.size() == 2);
			
			for(int i=0; i<ctnHoldList.size(); i++)
			{
				HoldStock cHoldStock = ctnHoldList.get(i);
				if(cHoldStock.stockID.equals("600001"))
				{
					CTest.EXPECT_STR_EQ(cHoldStock.createDate, "2017-10-10");
					CTest.EXPECT_LONG_EQ(cHoldStock.totalAmount, 300);
					CTest.EXPECT_LONG_EQ(cHoldStock.availableAmount, 300);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.curPrice, 2.00, 2);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.refPrimeCostPrice, 
							(100*1.60f + 200*2.0f)*(1+s_transactionCostsRatioBuy)/300 , 2);
				}
				if(cHoldStock.stockID.equals("300002"))
				{
					CTest.EXPECT_STR_EQ(cHoldStock.createDate, "2017-11-11");
					CTest.EXPECT_LONG_EQ(cHoldStock.totalAmount, 500);
					CTest.EXPECT_LONG_EQ(cHoldStock.availableAmount, 0);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.curPrice, 10.6, 2);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.refPrimeCostPrice, 
							(500*10.60f)*(1+s_transactionCostsRatioBuy)/500, 2);
				}
			}
		}
		
		cAccountController.close();
	}
	
	
	@CTest.test
	public static void test_accountDriver_sell()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" ,  new MockMarketOpe(), true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
		
		// buy
		cAccountController.setDateTime("2017-10-10", "14:00:01");
		acc.postTradeOrder(TRANACT.BUY, "600001", 100, 1.60f);
		acc.postTradeOrder(TRANACT.BUY, "600001", 200, 2.00f);
		cAccountController.newDayEnd();
		cAccountController.setDateTime("2017-11-11", "13:38:55");
		acc.postTradeOrder(TRANACT.BUY, "300002", 500, 10.6f);
		cAccountController.newDayEnd();
		
		// sell
		cAccountController.setDateTime("2017-11-13", "14:44:44");
		acc.postTradeOrder(TRANACT.SELL, "300002", 100, 10.0f);
		cAccountController.newDayEnd();
		
		// check
		{
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			CTest.EXPECT_TRUE(acc.getMoney(ctnMoney) == 0);
			double buyCost = 500*10.6f*s_transactionCostsRatioBuy;
			CTest.EXPECT_DOUBLE_EQ(ctnMoney.get(), 
					10*10000f - 100*1.60f*(1+s_transactionCostsRatioBuy) - 200*2.00f*(1+s_transactionCostsRatioBuy) - 500*10.6f*(1+s_transactionCostsRatioBuy) 
					+ 100*10.0f*(1-s_transactionCostsRatioSell) , 2);
			
			List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
			CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
			CTest.EXPECT_TRUE(ctnHoldList.size() == 2);
			
			for(int i=0; i<ctnHoldList.size(); i++)
			{
				HoldStock cHoldStock = ctnHoldList.get(i);
				if(cHoldStock.stockID.equals("600001"))
				{
					CTest.EXPECT_STR_EQ(cHoldStock.createDate, "2017-10-10");
					CTest.EXPECT_LONG_EQ(cHoldStock.totalAmount, 300);
					CTest.EXPECT_LONG_EQ(cHoldStock.availableAmount, 300);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.curPrice, 2.00, 2);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.refPrimeCostPrice, 
							(100*1.60f + 200*2.0f)*(1+s_transactionCostsRatioBuy)/300, 2);
				}
				if(cHoldStock.stockID.equals("300002"))
				{
					CTest.EXPECT_STR_EQ(cHoldStock.createDate, "2017-11-11");
					CTest.EXPECT_LONG_EQ(cHoldStock.totalAmount, 400);
					CTest.EXPECT_LONG_EQ(cHoldStock.availableAmount, 400);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.curPrice, 10.0, 2);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.refPrimeCostPrice, 
							(500*10.6f-100*10.0f+500*10.6f*s_transactionCostsRatioBuy+100*10.0f*s_transactionCostsRatioSell)/400, 2);
				}
			}
		}
		
		// sell
		cAccountController.setDateTime("2017-11-14", "09:50:01");
		acc.postTradeOrder(TRANACT.SELL, "300002", 400, 5.25f);
		cAccountController.newDayEnd();
		// check
		{
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			CTest.EXPECT_TRUE(acc.getMoney(ctnMoney) == 0);
			CTest.EXPECT_DOUBLE_EQ(ctnMoney.get(), 
					10*10000f - 100*1.60f*(1+s_transactionCostsRatioBuy) - 200*2.00f*(1+s_transactionCostsRatioBuy) - 500*10.6f*(1+s_transactionCostsRatioBuy) 
					+ 100*10.0f*(1-s_transactionCostsRatioSell) + 400*5.25f*(1-s_transactionCostsRatioSell), 2);
			
			List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
			CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
			CTest.EXPECT_TRUE(ctnHoldList.size() == 1);
			
			for(int i=0; i<ctnHoldList.size(); i++)
			{
				HoldStock cHoldStock = ctnHoldList.get(i);
				if(cHoldStock.stockID.equals("600001"))
				{
					CTest.EXPECT_STR_EQ(cHoldStock.createDate, "2017-10-10");
					CTest.EXPECT_LONG_EQ(cHoldStock.totalAmount, 300);
					CTest.EXPECT_LONG_EQ(cHoldStock.availableAmount, 300);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.curPrice, 2.00, 2);
					CTest.EXPECT_DOUBLE_EQ(cHoldStock.refPrimeCostPrice, 
							(100*1.60f + 200*2.0f)*(1+s_transactionCostsRatioBuy)/300, 2);
				}
			}
		}
		
		cAccountController.close();
	}
	
	@CTest.test
	public static void test_accountDriver_flushCurrentPrice()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" , new MockMarketOpe(), true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
		
		cAccountController.setDateTime("2017-10-10", "14:00:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 100, 1.60f);
		cAccountController.newDayEnd();
		
		cAccountController.setDateTime("2017-10-11", "14:30:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 200, 2.00f);
		cAccountController.newDayEnd();
		
		List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
		CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
		CTest.EXPECT_TRUE(ctnHoldList.size() == 1);
		for(int i=0; i<ctnHoldList.size(); i++)
		{
			HoldStock cHoldStock = ctnHoldList.get(i);
			if(cHoldStock.stockID.equals("600001"))
			{
				CTest.EXPECT_STR_EQ(cHoldStock.createDate, "2017-10-10");
				CTest.EXPECT_LONG_EQ(cHoldStock.totalAmount, 300);
				CTest.EXPECT_LONG_EQ(cHoldStock.availableAmount, 300);
				CTest.EXPECT_DOUBLE_EQ(cHoldStock.curPrice, 2.00, 2);
				CTest.EXPECT_DOUBLE_EQ(cHoldStock.refPrimeCostPrice, 
						(100*1.60f + 200*2.0f)*(1+s_transactionCostsRatioBuy)/300, 2);
				
				cAccountController.flushCurrentPrice("600001", 3.14f);
				CTest.EXPECT_DOUBLE_EQ(cHoldStock.curPrice, 3.14, 2);
			}
		}
		
		cAccountController.close();
	}
	
	@CTest.test
	public static void test_accountDriver_CommissionOrder()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" ,  new MockMarketOpe(), true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
		
		cAccountController.setDateTime("2017-10-10", "14:00:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 100, 1.60f);
		cAccountController.newDayEnd();
		
		cAccountController.setDateTime("2017-10-11", "14:30:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 200, 2.00f);
		cAccountController.setDateTime("2017-10-11", "14:32:01");
		acc.postTradeOrder(TRANACT.SELL, "600001", 100, 1.12f);
		
		List<CommissionOrder> ctnCommissionList = new ArrayList<CommissionOrder>();
		CTest.EXPECT_TRUE(acc.getCommissionOrderList(ctnCommissionList) == 0);
		CTest.EXPECT_LONG_EQ(ctnCommissionList.size(), 2);

		CommissionOrder cCommissionOrder0 = ctnCommissionList.get(0);
		CTest.EXPECT_STR_EQ(cCommissionOrder0.date, "2017-10-11");
		CTest.EXPECT_STR_EQ(cCommissionOrder0.time, "14:30:01");
		CTest.EXPECT_LONG_EQ(cCommissionOrder0.amount, 200);
		CTest.EXPECT_DOUBLE_EQ(cCommissionOrder0.price, 2.0f, 2);
		CTest.EXPECT_TRUE(0 == cCommissionOrder0.tranAct.compareTo(TRANACT.BUY));
		
		CommissionOrder cCommissionOrder1 = ctnCommissionList.get(1);
		CTest.EXPECT_STR_EQ(cCommissionOrder1.date, "2017-10-11");
		CTest.EXPECT_STR_EQ(cCommissionOrder1.time, "14:32:01");
		CTest.EXPECT_LONG_EQ(cCommissionOrder1.amount, 100);
		CTest.EXPECT_DOUBLE_EQ(cCommissionOrder1.price, 1.12f, 2);
		CTest.EXPECT_TRUE(0 == cCommissionOrder1.tranAct.compareTo(TRANACT.SELL));
		
		cAccountController.newDayEnd();
		cAccountController.close();
		
	}
	
	@CTest.test
	public static void test_accountDriver_DealOrder()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" ,  new MockMarketOpe(), true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
		
		cAccountController.setDateTime("2017-10-10", "14:00:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 100, 1.60f);
		cAccountController.newDayEnd();
		
		cAccountController.setDateTime("2017-10-11", "14:30:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 200, 2.00f);
		cAccountController.setDateTime("2017-10-11", "14:32:01");
		acc.postTradeOrder(TRANACT.SELL, "600001", 100, 1.12f);
		
		List<DealOrder> ctnDealList = new ArrayList<DealOrder>();
		CTest.EXPECT_TRUE(acc.getDealOrderList(ctnDealList) == 0);
		CTest.EXPECT_LONG_EQ(ctnDealList.size(), 2);

		if(ctnDealList.size() == 2)
		{
			DealOrder cDealOrder0 = ctnDealList.get(0);
			CTest.EXPECT_STR_EQ(cDealOrder0.date, "2017-10-11");
			CTest.EXPECT_STR_EQ(cDealOrder0.time, "14:30:01");
			CTest.EXPECT_TRUE(0 == cDealOrder0.tranAct.compareTo(TRANACT.BUY));
			CTest.EXPECT_STR_EQ(cDealOrder0.stockID, "600001");
			CTest.EXPECT_LONG_EQ(cDealOrder0.amount, 200);
			CTest.EXPECT_DOUBLE_EQ(cDealOrder0.price, 2.0f, 2);
			CTest.EXPECT_DOUBLE_EQ(cDealOrder0.cost, cDealOrder0.amount*cDealOrder0.price*s_transactionCostsRatioBuy, 2);

			
			DealOrder cDealOrder1 = ctnDealList.get(1);
			CTest.EXPECT_STR_EQ(cDealOrder1.date, "2017-10-11");
			CTest.EXPECT_STR_EQ(cDealOrder1.time, "14:32:01");
			CTest.EXPECT_TRUE(0 == cDealOrder1.tranAct.compareTo(TRANACT.SELL));
			CTest.EXPECT_STR_EQ(cDealOrder1.stockID, "600001");
			CTest.EXPECT_LONG_EQ(cDealOrder1.amount, 100);
			CTest.EXPECT_DOUBLE_EQ(cDealOrder1.price, 1.12f, 2);
			CTest.EXPECT_DOUBLE_EQ(cDealOrder1.cost, 
					100*1.12f*s_transactionCostsRatioSell, 2);
		}
		
		String dumpInfoBeforeEnd = acc.dump();
		CLog.debug("TEST", "\n%s", dumpInfoBeforeEnd);
		
		cAccountController.newDayEnd();
		
		String dumpInfoAfterEnd = acc.dump();
		CLog.debug("TEST", "\n%s", dumpInfoAfterEnd);
		
		cAccountController.close();
		
	}
	
	@CTest.test
	public static void test_accountDriver_inputcheck_buy_sell()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" ,  new MockMarketOpe(), true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
		
		{
			cAccountController.setDateTime("2017-10-10", "14:00:01");
			cAccountController.newDayBegin();
			int ret = acc.postTradeOrder(TRANACT.BUY, "600001", 20000, 1.5f);
			CTest.EXPECT_LONG_EQ(ret, 0);
			ret = acc.postTradeOrder(TRANACT.BUY, "600002", 20000, 1.5f);
			CTest.EXPECT_LONG_EQ(ret, 0);
			ret = acc.postTradeOrder(TRANACT.BUY, "300003", 20000, 1.5f);
			CTest.EXPECT_LONG_EQ(ret, 0);
			
			ret = acc.postTradeOrder(TRANACT.BUY, "300004", 20000, 1.5f);
			CTest.EXPECT_LONG_EQ(ret, -1);
			
			List<CommissionOrder> ctnCommissionList = new ArrayList<CommissionOrder>();
			CTest.EXPECT_TRUE(acc.getCommissionOrderList(ctnCommissionList) == 0);
			CTest.EXPECT_LONG_EQ(ctnCommissionList.size(), 3);
			cAccountController.newDayEnd();
		}
		
		{
			cAccountController.setDateTime("2017-10-11", "14:00:01");
			cAccountController.newDayBegin();
			int ret = acc.postTradeOrder(TRANACT.SELL, "600001", 20000, 1.5f);
			CTest.EXPECT_LONG_EQ(ret, 0);
			ret = acc.postTradeOrder(TRANACT.SELL, "600002", 20000, 1.5f);
			CTest.EXPECT_LONG_EQ(ret, 0);
			ret = acc.postTradeOrder(TRANACT.SELL, "300003", 20000, 1.5f);
			CTest.EXPECT_LONG_EQ(ret, 0);
			
			ret = acc.postTradeOrder(TRANACT.SELL, "300004", 20000, 1.5f);
			CTest.EXPECT_LONG_EQ(ret, -1);
			
			List<CommissionOrder> ctnCommissionList = new ArrayList<CommissionOrder>();
			CTest.EXPECT_TRUE(acc.getCommissionOrderList(ctnCommissionList) == 0);
			CTest.EXPECT_LONG_EQ(ctnCommissionList.size(), 3);
			cAccountController.newDayEnd();
		}
		
		cAccountController.close();
		
	}
	
	@CTest.test
	public static void test_accountDriver_performance()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" ,  new MockMarketOpe(), true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
		
		cAccountController.setDateTime("2017-10-10", "14:00:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 100, 1.60f);
		acc.postTradeOrder(TRANACT.BUY, "600001", 200, 2.00f);
		acc.postTradeOrder(TRANACT.BUY, "300002", 500, 10.6f);
		
		List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
		CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
		CTest.EXPECT_TRUE(ctnHoldList.size() == 2);
		
		int iTestTimes=100*10000;
		int iTest = 0;
		for(; iTest<10000*100; iTest++)
		{
			cAccountController.setDateTime("2017-10-10", "14:00:30");
			for(int i=0; i<ctnHoldList.size(); i++)
			{
				HoldStock cHoldStock = ctnHoldList.get(i);
				if(cHoldStock.stockID.equals("600001"))
				{
					cAccountController.flushCurrentPrice("600001", 3.14f);
				}
				if(cHoldStock.stockID.equals("300002"))
				{
					cAccountController.flushCurrentPrice("300002", 6.28f);
				}
			}
		}
		CTest.EXPECT_LONG_EQ(iTest, iTestTimes);
		
		cAccountController.newDayEnd();
		
		cAccountController.close();
	}
	
	@CTest.test
	public static void test_accountDriver_totalassets_singleHoldStock_refProfit()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001" ,  new MockMarketOpe(), true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
		
		cAccountController.setDateTime("2017-10-10", "14:00:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 1000, 16.8f);
		acc.postTradeOrder(TRANACT.BUY, "600001", 2000, 20.0f);
		acc.postTradeOrder(TRANACT.BUY, "300002", 500, 10.6f);
		cAccountController.newDayEnd();
		
		List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
		CTest.EXPECT_STR_EQ(acc.date(), "2017-10-10");
		CTest.EXPECT_STR_EQ(acc.time(), "14:00:01");
		CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
		CTest.EXPECT_TRUE(ctnHoldList.size() == 2);
		
		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		CTest.EXPECT_LONG_EQ(acc.getTotalAssets(ctnTotalAssets), 0);
		CTest.EXPECT_DOUBLE_EQ(ctnTotalAssets.get(), 10*10000f+1000*3.2f 
				-1000*16.8*(s_transactionCostsRatioBuy)-2000*20*(s_transactionCostsRatioBuy)-500*10.6*(s_transactionCostsRatioBuy), 2);
		
		CObjectContainer<HoldStock> cHoldStock = new CObjectContainer<HoldStock>();
		CTest.EXPECT_LONG_EQ(acc.getHoldStock("600001", cHoldStock), 0);
		CTest.EXPECT_LONG_EQ(cHoldStock.get().availableAmount, 3000);
		CTest.EXPECT_DOUBLE_EQ(cHoldStock.get().refProfit(), 
				1000*3.2f - (1000*16.8f+2000*20.0f)*s_transactionCostsRatioBuy, 2);
		CTest.EXPECT_DOUBLE_EQ(cHoldStock.get().refProfitRatio(), 
				cHoldStock.get().refProfit()/((1000*16.8f+2000*20.0f)+(1000*16.8f+2000*20.0f)*s_transactionCostsRatioBuy), 4);
	
		cAccountController.close();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CLog.config_setTag("TEST", true);
		CLog.config_setTag("ACCOUNT", false);
		CTest.ADD_TEST(TestAccountController.class);
		CTest.RUN_ALL_TESTS("TestAccountController.");
		CLog.debug("TEST", "END");
		CSystem.stop();
	}
}
