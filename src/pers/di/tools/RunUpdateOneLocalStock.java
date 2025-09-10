package pers.di.tools;

import pers.di.dataprovider.DataProvider;

public class RunUpdateOneLocalStock {
    public static void main(String[] args) {
        DataProvider.getInstance().updateOneLocalStocks("600000", 65536);
    }
}
