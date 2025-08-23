package pers.di.dataengine.internal;

import pers.di.dataengine.IEngineListener;
import pers.di.dataengine.StockDataEngine;

public class DStockDataEngineImpl extends StockDataEngine {
    @Override
    public int config(String key, String value) { return -1;}

    @Override
    public int registerListener(IEngineListener listener) { return -1;}

    @Override
    public int unRegisterListener(IEngineListener listener) { return -1;}

    @Override
    public int run() { return -1;}
}
