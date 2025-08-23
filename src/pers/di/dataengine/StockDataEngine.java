package pers.di.dataengine;

import pers.di.dataengine.internal.DStockDataEngineImpl;
import pers.di.dataprovider.DataProvider;
import pers.di.dataprovider.internal.DataProviderImpl;

public class StockDataEngine {
    public int config(String key, String value) { return -1;}
    public int registerListener(IEngineListener listener) { return -1;}
    public int unRegisterListener(IEngineListener listener) { return -1;}
    public int run() { return -1;}

    // singleton
    private static volatile StockDataEngine instance;
    protected StockDataEngine() {
    }
    public static StockDataEngine getInstance() {
        if (instance == null) {
            synchronized (StockDataEngine.class) {
                if (instance == null) {
                    instance = new DStockDataEngineImpl();
                }
            }
        }
        return instance;
    }
}
