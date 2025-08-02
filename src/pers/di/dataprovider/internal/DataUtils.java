package pers.di.dataprovider.internal;

import java.util.ArrayList;
import java.util.List;

import pers.di.model.KLine;

public class DataUtils {
    public static String getLocalStockIDLatestDate(DataStorage storage, String stockID) {
        List<KLine> ctnKLine = new ArrayList<KLine>();
        int errKLine = storage.getLocalStockIDKLineList(stockID, ctnKLine);
        if (0 == errKLine && ctnKLine.size() > 0) {
            String latestDate = ctnKLine.get(ctnKLine.size() - 1).date;
            return latestDate;
        }
        return "0000-00-00"; // 没有数据 返回0000-00-00
    }
}
