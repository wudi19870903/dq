package pers.di.account;

import java.util.ArrayList;
import java.util.List;

import pers.di.account.internal.DefaultMockSyncMarketOpe;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSystem;
import pers.di.common.CTest;

public class TestMockAccountMockMarketOpe {
	private static final boolean DEBUG_TESTCASE_LOG = false;
	private static void TESTCASE_LOG(String format, Object... args) {
		if (DEBUG_TESTCASE_LOG) CLog.info("TEST", String.format(format, args));
	}
    public static String s_accountDataRoot = CSystem.getRWRoot() + "\\account";
	
	@CTest.test
	public static void test_buy_and_sell()
	{
		AccountController cAccountController = new AccountController(s_accountDataRoot);
		cAccountController.open("mock001", true);
		cAccountController.reset(10*10000f);
		
		IAccount acc = cAccountController.account();
        double currentMoney = 0;
		
		cAccountController.setDateTime("2017-10-10", "14:00:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 600, 10.6f);
		acc.postTradeOrder(TRANACT.BUY, "600001", 300, 12.1f);
		cAccountController.newDayEnd();
		//check
		{
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			CTest.EXPECT_TRUE(acc.getMoney(ctnMoney) == 0);
			CTest.EXPECT_DOUBLE_EQ(ctnMoney.get(), 
                10*10000f 
                    - 600*10.6f - DefaultMockSyncMarketOpe.calcBuyCost(600, 10.6f)
                    - 300*12.1f - DefaultMockSyncMarketOpe.calcBuyCost(300, 12.1f) , 2);
			List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
			CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
			CTest.EXPECT_TRUE(ctnHoldList.size() == 1);
            currentMoney = ctnMoney.get();
		}
		
		cAccountController.setDateTime("2017-10-11", "13:00:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.BUY, "600001", 100, 11.5f);
		acc.postTradeOrder(TRANACT.BUY, "600002", 100, 24.88f);
		acc.postTradeOrder(TRANACT.BUY, "600002", 200, 23.05f);
		acc.postTradeOrder(TRANACT.BUY, "600001", 500, 9.89f);
		//check
		{
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			CTest.EXPECT_TRUE(acc.getMoney(ctnMoney) == 0);
			double availableMoney = currentMoney
                - 100*11.5f - DefaultMockSyncMarketOpe.calcBuyCost(100, 11.5f)
                - 100*24.88f - DefaultMockSyncMarketOpe.calcBuyCost(100, 24.88f)
                - 200*23.05f - DefaultMockSyncMarketOpe.calcBuyCost(200, 23.05f)
                - 500*9.89f - DefaultMockSyncMarketOpe.calcBuyCost(500, 9.89f);
			CTest.EXPECT_DOUBLE_EQ(ctnMoney.get(), availableMoney, 2);
			List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
			CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
			CTest.EXPECT_TRUE(ctnHoldList.size() == 2);
			List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
			CTest.EXPECT_TRUE(acc.getCommissionOrderList(ctnCommissionOrderList) == 0);
            CTest.EXPECT_TRUE(ctnCommissionOrderList.size() == 4);
			for(int i=0; i<ctnCommissionOrderList.size(); i++)
			{
				CommissionOrder cCommissionOrder = ctnCommissionOrderList.get(i);
                CTest.EXPECT_LONG_EQ(cCommissionOrder.dealAmount, cCommissionOrder.amount);
			}
            currentMoney = ctnMoney.get();
		}
		cAccountController.newDayEnd();
				
		cAccountController.setDateTime("2017-10-12", "14:30:01");
		cAccountController.newDayBegin();
		acc.postTradeOrder(TRANACT.SELL, "600001", 200, 8.68f);
		acc.postTradeOrder(TRANACT.SELL, "600001", 100, 10.85f);
		acc.postTradeOrder(TRANACT.SELL, "600001", 100, 8.18f);
		//check
		{
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			CTest.EXPECT_TRUE(acc.getMoney(ctnMoney) == 0);
			double availableMoney = currentMoney
                + 200*8.68f - DefaultMockSyncMarketOpe.calcSellCost(200, 8.68f)
                + 100*10.85f - DefaultMockSyncMarketOpe.calcSellCost(100, 10.85f)
                + 100*8.18f - DefaultMockSyncMarketOpe.calcSellCost(100, 8.18f);
			CTest.EXPECT_DOUBLE_EQ(ctnMoney.get(), availableMoney, 2);
			List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
			CTest.EXPECT_TRUE(acc.getHoldStockList(ctnHoldList) == 0);
			CTest.EXPECT_TRUE(ctnHoldList.size() == 2);
			List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
			CTest.EXPECT_TRUE(acc.getCommissionOrderList(ctnCommissionOrderList) == 0);
			for(int i=0; i<ctnCommissionOrderList.size(); i++)
			{
				CommissionOrder cCommissionOrder = ctnCommissionOrderList.get(i);
				if(0 == Double.compare(cCommissionOrder.price, 8.68))
				{
					CTest.EXPECT_LONG_EQ(cCommissionOrder.dealAmount, 200);
				}
			}
		}
		cAccountController.newDayEnd();
		
		cAccountController.close();

        TESTCASE_LOG(cAccountController.account().dump());
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CSystem.start();
		CTest.ADD_TEST(TestMockAccountMockMarketOpe.class);
		CTest.RUN_ALL_TESTS("TestMockAccountMockMarketOpe.");
		CSystem.stop();
	}
}
