package pers.di.dquant;

import pers.di.common.CListObserver;
import pers.di.dataengine.DAContext;
import pers.di.model.KLine;

public class IStockPickStrategy {
    public boolean onUserPick(DAContext context, String stockID, CListObserver<KLine> kLines) {
        return false;
    };
}
