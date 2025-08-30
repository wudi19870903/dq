package pers.di.dquant;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

public class PickerReport {
    public PickerReport () {
        pickList = new ArrayList<Pair<String, String>>();
    }
    public void reset() {
        pickList.clear();
    }
    
    public List<Pair<String, String>> pickList; // 选择列表（日期，股票ID）
}
