package pers.di.dataengine;

import pers.di.dataengine.internal.StockDataEngineImpl;
import pers.di.dataprovider.DataProvider;
import pers.di.dataprovider.internal.DataProviderImpl;

public class StockDataEngine {

    /*
	 * 配置量化引擎
	 * 
	 * key: "TriggerMode" 触发模式
	 *     value: "HistoryTest XXXX-XX-XX XXXX-XXXX-XX" 历史回测
	 *     value: "HistoryTest XXXX-XX-XX" 历史回测
	 */
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
                    instance = new StockDataEngineImpl();
                }
            }
        }
        return instance;
    }
}
