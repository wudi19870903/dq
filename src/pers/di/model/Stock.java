package pers.di.model;

import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CObjectObserver;

public class Stock {
	public String ID;
	public String name;
	public String date;
	public double price;
	public CListObserver<KLine> dayKLines;
}
