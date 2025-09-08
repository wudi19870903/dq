package pers.di.dquant.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import pers.di.account.HoldStock;
import pers.di.account.IAccount;
import pers.di.account.TRANACT;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.dataengine.DAContext;
import pers.di.model.KLine;
import pers.di.model.StockUtils;

public class SimpleAccountBuySellStrategy {
    public static final boolean ENABLE = true;
    public static final long MAX_HOLD_STOCK_COUNT = 10; // 参数：最大持股数量
    public static final long MAX_HOLD_STOCK_DAYS = 20; // 参数：最大持股天数
    public static final double STOP_WIN_RATE_THRESHOLD = 0.10; // 策略参数：止盈收益
    public static final double STOP_LOSE_RATE_THRESHOLD = 0.10; // 策略参数：止损收益

    public SimpleAccountBuySellStrategy(IAccount acc) {
        mAccount = acc;
    }

    public void trySell(DAContext context) {
        if (!ENABLE) return;

        List<HoldStock> listHoldStock = new ArrayList<HoldStock>();
        mAccount.getHoldStockList(listHoldStock);
        for (int i = 0; i < listHoldStock.size(); i++) { 
            HoldStock holdStock = listHoldStock.get(i);
            CListObserver<KLine> klineList = context.getDayKLinesBackwardAdjusted(holdStock.stockID);
            int holdday = StockUtils.getDayCountBetweenBeginEnd(klineList, holdStock.createDate, mAccount.date());
            double winRate = (holdStock.curPrice - holdStock.refPrimeCostPrice)/holdStock.refPrimeCostPrice;
            if (holdday >= MAX_HOLD_STOCK_DAYS
                || winRate >= STOP_WIN_RATE_THRESHOLD
                || winRate <= -STOP_LOSE_RATE_THRESHOLD) {
                CLog.info("DQUANT", "SimpleAccountBuySellStrategy.doSell %s %s %s %d %.2f",
                    mAccount.date() , mAccount.time(),
                    holdStock.stockID, holdStock.availableAmount, holdStock.curPrice);
                mAccount.postTradeOrder(TRANACT.SELL, holdStock.stockID, holdStock.availableAmount, holdStock.curPrice);
            }
        }
    }

    public void tryBuy(List<Entry<String, Double>> list) {
        if (!ENABLE) return;

        // 将买入列表打乱
        Collections.shuffle(list);

        List<HoldStock> listHoldStock = new ArrayList<HoldStock>();
        mAccount.getHoldStockList(listHoldStock);

        // 超过最大股票数量，则不进行买入
        long couldBuyMaxCount = MAX_HOLD_STOCK_COUNT - listHoldStock.size();
        if (couldBuyMaxCount <= 0) {
            return;
        }
        // 剩余资金小于1000不进行买入
        CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
        mAccount.getMoney(ctnMoney);
        double availableMoney = ctnMoney.get() - 1000;
        if (availableMoney < 0) {
            return;
        }
        // 计算每个票的买入资金
        double buyMoneyForOneStock = availableMoney / couldBuyMaxCount;

        // 循环买入
        long buyCount = Math.min(couldBuyMaxCount, list.size());
        for (int i = 0; i < buyCount; i++) {
            Entry<String, Double> buyItem = list.get(i);
            String stockID = buyItem.getKey();
            if (inHoldStock(stockID, listHoldStock)) {
                // 忽略已经持仓的
                continue;
            }
            double price = buyItem.getValue();
            int amount = (int)(buyMoneyForOneStock / price) / 100 * 100;
            CLog.info("DQUANT", "SimpleAccountBuySellStrategy.doBuy %s %s %s %d %.2f",
                mAccount.date() , mAccount.time(),
                stockID, amount, price);
            mAccount.postTradeOrder(TRANACT.BUY, stockID, amount, price);
            
        }
    }

    private boolean inHoldStock(String stockID, List<HoldStock> listHoldStock) {
        for (int i = 0; i < listHoldStock.size(); i++) { 
            HoldStock holdStock = listHoldStock.get(i);
            if (holdStock.stockID.equals(stockID)) {
                return true;
            }
        }
        return false;
    }

    private IAccount mAccount;
}

