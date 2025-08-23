package pers.di.dataengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.chart.PieChart.Data;
import pers.di.common.CListObserver;
import pers.di.dataprovider.DataProvider;
import pers.di.model.KLine;
import pers.di.model.StockUtils;

public class DAContext {

    public DAContext () {
        mDate = "2025-01-01";
        mStockIDList = new ArrayList<String>();
        mDayKLinesMap = new HashMap<String, List<KLine>>();
    }
    public String date() {
        return mDate;
    }
    public void setDate(String date) {
        mDate = date;
    }

    public CListObserver<String> getAllStockID() {
        if (mStockIDList.size() <= 0) {
            DataProvider.getInstance().getLocalAvailableDayKlinesStockIDList(mStockIDList);
        }
        CListObserver<String> stockIdsObserver = new CListObserver<String>();
        stockIdsObserver.build(mStockIDList, 0, mStockIDList.size());
        return stockIdsObserver;
    }

    public CListObserver<KLine> getDayKLines(String stockID) {
        if (!mDayKLinesMap.containsKey(stockID)) {
            List<KLine> klines = new ArrayList<KLine>();
            DataProvider.getInstance().getDayKLinesForwardAdjusted(stockID, klines);
            mDayKLinesMap.put(stockID, klines);
        }
        List<KLine> klines = mDayKLinesMap.get(stockID);
        int endIdx = StockUtils.indexDayKBeforeDate(klines, mDate, true);
        CListObserver<KLine> klinesObserver = new CListObserver<KLine>();
        klinesObserver.build(klines, 0, endIdx + 1);
        return klinesObserver;
    }

    private String mDate;
    private List<String> mStockIDList;
    private Map<String, List<KLine>> mDayKLinesMap;
}
