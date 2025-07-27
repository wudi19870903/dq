package pers.di.model;

/*
 * 分红派息因子
 * 日期-送股-转送-派息
 */
public class DividendPayout implements Comparable
{
	public String date;
	public double songGu;
	public double zhuanGu;
	public double paiXi;
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		DividendPayout sdto = (DividendPayout)o;
	    return this.date.compareTo(sdto.date);
	}
}
