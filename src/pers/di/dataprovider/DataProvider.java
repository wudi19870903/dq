package pers.di.dataprovider;

import java.util.List;
import pers.di.dataprovider.internal.*;

public class DataProvider {
    public String dataRoot() { return null;}
    public int updateAllLocalStocks() { return -1;}
    public int updateOneLocalStocks(String stockID) { return -1;}
    public int getAllStockIDList(List<String> list) { return -1;}

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
