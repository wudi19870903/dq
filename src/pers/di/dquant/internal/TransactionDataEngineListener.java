package pers.di.dquant.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.awt.Color;

import pers.di.account.AccountController;
import pers.di.account.HoldStock;
import pers.di.account.IAccount;
import pers.di.common.CImageCurve;
import pers.di.common.CImageCurve.CurvePoint;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
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
        mSimpleAccountBuySellStrategy = new SimpleAccountBuySellStrategy(mAccount);
        mPickList = new ArrayList<Entry<String, String>>();
        mRefRatioPath = CSystem.getRunSessionRoot() + "\\refRatio.jpg";
        mCImageCurve = new CImageCurve(1600, 900, mRefRatioPath);
        mPoiListSZZS = new ArrayList<CurvePoint>();
        mPoiListAccAssets = new ArrayList<CurvePoint>();
    }
    
    @Override
	public void onTradingDayFinish(DAContext context) {
        if (null == mStockPickStrategy) {
            return;
        }
        // ***************************************************************************
        // 新交易日开始 09:30:00
        mAccountController.newDayBegin();
        mAccountController.setDateTime(context.date(), "09:30:00");
        updateAccountAllStocksForOpenPrice(context, mAccountController);

        // 卖出检查并实施
        mSimpleAccountBuySellStrategy.trySell(context);

        // 上一交易日被选中，整理出买入列表
        List<Entry<String, Double>> buyList = new ArrayList<Entry<String, Double>>();
        for (int i = 0; i < mPickList.size(); i++) { 
            Entry<String, String> pickPair = mPickList.get(i);
            String pickDate = pickPair.getKey();
            String pickStockID = pickPair.getValue();
            // 注意：此时必须使用后复权来回测历史数据，只有这样历史的涨跌幅才不失真
            CListObserver<KLine> klineList = context.getDayKLinesBackwardAdjusted(pickStockID);
            KLine end = klineList.end();
            KLine preEnd = klineList.get(klineList.size() - 2);
            if (preEnd.date.equals(pickDate)) {
                // CLog.info("DQUANT", "StockTriggerBuy date:%s stockID:%s", context.date(), pickStockID);
                buyList.add(new java.util.AbstractMap.SimpleEntry<String, Double>(pickStockID, end.open));
            }
        }
        mPickList.clear();

        // 买入检查并实施
        mSimpleAccountBuySellStrategy.tryBuy(buyList);

        // ***************************************************************************
        // 新交易日结束 15:00:00
        mAccountController.newDayEnd();
        mAccountController.setDateTime(context.date(), "15:00:00");
        updateAccountAllStocksForClosePrice(context, mAccountController);

        // 根据策略添加选入列表
        // CLog.info("DQUANT", "PickerDataEngineListener date:%s", context.date());
        for (int i = 0; i < context.getAllStockID().size(); i++) {
            String stockID = context.getAllStockID().get(i);
            CListObserver<KLine> klineList = context.getDayKLinesBackwardAdjusted(stockID);
            boolean bPick = mStockPickStrategy.onUserPick(context, stockID, klineList);
            if (bPick) {
                CLog.info("DQUANT", "StockPickStrategy date:%s stockID:%s %.2f",
                    context.date(), stockID , klineList.end().close);
                Entry<String, String> pickPair = new java.util.AbstractMap.SimpleEntry<>(context.date(), stockID);
                mPickList.add(pickPair);
            }
        }

        // 更新当日参考曲线
        CListObserver<KLine> klineListSZZS = context.getDayKLinesBackwardAdjusted("999999");
        mPoiListSZZS.add(new CurvePoint(klineListSZZS.size(), klineListSZZS.end().close));
        CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
        mAccount.getTotalAssets(ctnTotalAssets);
        mPoiListAccAssets.add(new CurvePoint(klineListSZZS.size(), ctnTotalAssets.get()));
    }

    @Override
    public void onUnInitialize(DAContext context) {
        // 打印账户信息
        CLog.info("DQUANT", "onUnInitialize");
        CLog.info("DQUANT", mAccount.dump());
        // 生成参考曲线
        CLog.info("DQUANT", "ProfitCurve: %s", mRefRatioPath);
        mCImageCurve.setColor(Color.ORANGE);
		mCImageCurve.writeLogicCurveSameRatio(mPoiListSZZS);
        mCImageCurve.setColor(Color.GREEN);
		mCImageCurve.writeLogicCurveSameRatio(mPoiListAccAssets);
        mCImageCurve.setColor(Color.BLACK);
		mCImageCurve.writeAxis();
		mCImageCurve.GenerateImage();
    };

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
            double price = context.getDayKLinesBackwardAdjusted(stockID).end().open;
            accountController.flushCurrentPrice(stockID, price);
        }
    }

    private void updateAccountAllStocksForClosePrice(DAContext context, AccountController accountController) {
        List<HoldStock> ctnHoldList = new ArrayList<HoldStock>();
        mAccount.getHoldStockList(ctnHoldList);
        for (int i = 0; i < ctnHoldList.size(); i++) {
            HoldStock holdStock = ctnHoldList.get(i);
            String stockID = holdStock.stockID;
            double price = context.getDayKLinesBackwardAdjusted(stockID).end().close;
            accountController.flushCurrentPrice(stockID, price);
        }
    }

    private IStockPickStrategy mStockPickStrategy;
    private AccountController mAccountController;
    private IAccount mAccount;
    private SimpleAccountBuySellStrategy mSimpleAccountBuySellStrategy;
    private String mRefRatioPath;
    private CImageCurve mCImageCurve;
    private List<CurvePoint> mPoiListSZZS;
    private List<CurvePoint> mPoiListAccAssets;
    /* 
     * 选择列表（日期，股票ID）
    */ 
    public List<Entry<String, String>> mPickList;
}
