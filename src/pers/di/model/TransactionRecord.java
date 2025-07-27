package pers.di.model;

/*
 * 日内交易明细
 * 时间-价格-成交量
 */
public class TransactionRecord implements Comparable
{
	public String time;
	public double price;
	public double volume; 
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		TransactionRecord sdto = (TransactionRecord)o;
	    return this.time.compareTo(sdto.time);
	}
}