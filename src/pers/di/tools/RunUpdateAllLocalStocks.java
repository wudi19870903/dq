package pers.di.tools;

import pers.di.dataprovider.DataProvider;

public class RunUpdateAllLocalStocks {
    public static void main(String[] args) {
        DataProvider.getInstance().updateAllLocalStocks();
    }
}
