package pers.di.dquant.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import pers.di.account.AccountController;
import pers.di.account.HoldStock;
import pers.di.account.IAccount;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAContext;
import pers.di.dataengine.IEngineListener;
import pers.di.dquant.IStockPickStrategy;
import pers.di.model.KLine;

public class TransactionDataEngineListener extends IEngineListener {
    public TransactionDataEngineListener() {
        mAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
        mAccountController.open("mock001", true);
        mAccountController.reset(100000.0);
        mAccount = mAccountController.account();
        mPickList = new ArrayList<Pair<String, String>>();
    }
    
    @Override
	public void onTradingDayFinish(DAContext context) {
        if (null == mStockPickStrategy) {
            return;
        }
        // 账户新交易日开始
        mAccountController.newDayBegin();
        mAccountController.setDateTime(context.date(), "09:30:00");
        updateAccountAllStocksForOpenPrice(context, mAccountController);

        // 上一交易日被选中，那么尝试开盘价买入交易
        for (int i = 0; i < mPickList.size(); i++) { 
            Pair<String, String> pickPair = mPickList.get(i);
            String pickDate = pickPair.getKey();
            String pickStockID = pickPair.getValue();
            CListObserver<KLine> klineList = context.getDayKLines(pickStockID);
            KLine preEnd = klineList.get(klineList.size() - 2);
            if (preEnd.date.equals(pickDate)) {
                CLog.info("DQUANT", "StockTriggerBuy date:%s stockID:%s", 
                    context.date(), pickStockID);
            }
        }
        mPickList.clear();

        // 账户新交易日结束
        mAccountController.newDayEnd();
        mAccountController.setDateTime(context.date(), "15:00:00");
        updateAccountAllStocksForClosePrice(context, mAccountController);

        // 根据策略添加选入列表
        // CLog.info("DQUANT", "PickerDataEngineListener date:%s", context.date());
        for (int i = 0; i < context.getAllStockID().size(); i++) {
            String stockID = context.getAllStockID().get(i);
            CListObserver<KLine> klineList = context.getDayKLines(stockID);
            boolean bPick = mStockPickStrategy.onUserPick(context, stockID, klineList);
            if (bPick) {
                CLog.info("DQUANT", "StockPickStrategy date:%s stockID:%s", 
                    context.date(), stockID);
                Pair<String, String> pickPair = new Pair<String, String>(context.date(), stockID);
                mPickList.add(pickPair);
            }
        }
    }

    public void setStockPickStrategy(IStockPickStrategy strategy) {
        CLog.info("DQUANT", "setStockPickStrategy %s", strategy.getClass().getName());
    	mStockPickStrategy = strategy;
    }

    private void updateAccountAllStocksForOpenPrice(DAContext context, AccountController accountController) {
        List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
        mAccount.getHoldStockList(ctnHoldList);
        for (int i = 0; i < ctnHoldList.size(); i++) {
            HoldStock holdStock = ctnHoldList.get(i);
            String stockID = holdStock.stockID;
            double price = context.getDayKLines(stockID).end().open;
            accountController.flushCurrentPrice(stockID, price);
        }
    }

    private void updateAccountAllStocksForClosePrice(DAContext context, AccountController accountController) {
        List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
        mAccount.getHoldStockList(ctnHoldList);
        for (int i = 0; i < ctnHoldList.size(); i++) {
            HoldStock holdStock = ctnHoldList.get(i);
            String stockID = holdStock.stockID;
            double price = context.getDayKLines(stockID).end().close;
            accountController.flushCurrentPrice(stockID, price);
        }
    }

    private IStockPickStrategy mStockPickStrategy;
    private AccountController mAccountController;
    private IAccount mAccount;
    /* 
     * 选择列表（日期，股票ID）
    */ 
    public List<Pair<String, String>> mPickList;
}
