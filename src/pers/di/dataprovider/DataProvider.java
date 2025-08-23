package pers.di.dataprovider;

import java.util.List;
import java.util.Map;

import pers.di.dataprovider.internal.*;
import pers.di.model.KLine;

public class DataProvider {
    // 数据跟目录
    public String dataRoot() { return null;}
    public void setDataRoot(String dataRoot) {}

    // 更新所有本地股票数据
    public int updateAllLocalStocks() { return -1;}

    // 更新一个本地股票数据
    public int updateOneLocalStocks(String stockID) { return -1;}

    // 获取本地股票ID列表（从本地数据excel中读取的)
    public int getLocalAllStockIDList(List<String> list) { return -1;}

    // 获取本地股票ID列表和名字
    public int getLocalAllStockIDNameMap(Map<String, String> map) { return -1;}

    // 获取本地有效日K线股票ID列表（遍历数据目录下存在日K线的股票ID)
    public int getLocalAvailableDayKlinesStockIDList(List<String> list) { return -1;}
    // 获取本地股票日K线列表
    public int getLocalDayKLines(String stockID, List<KLine> container) {return -1;}
    
    // 获取本地股票日K线列表-前复权
    public int getDayKLinesForwardAdjusted(String stockID, List<KLine> container) {return -1;}
    
    // 获取本地股票日K线列表-后复权
    public int getDayKLinesBackwardAdjusted(String stockID, List<KLine> container) {return -1;}

    // singleton
    private static volatile DataProvider instance;
    protected DataProvider() {
    }
    public static DataProvider getInstance() {
        if (instance == null) {
            synchronized (DataProvider.class) {
                if (instance == null) {
                    instance = new DataProviderImpl();
                }
            }
        }
        return instance;
    }
}
